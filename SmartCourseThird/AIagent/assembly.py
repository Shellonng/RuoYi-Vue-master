from __future__ import annotations

from collections import Counter, defaultdict
from dataclasses import dataclass
from typing import Dict, Iterable, List, Optional

from db import Question, QuestionBankRepository, QuestionSearchFilters


# 题型基础权重配置（用于智能分配分数）
QUESTION_TYPE_WEIGHTS = {
    "single": 1.0,      # 单选题 - 基础权重
    "multiple": 1.5,    # 多选题 - 稍高
    "true_false": 0.8,         # 判断题 - 较低
    "blank": 1.2,         # 填空题 - 中等
    "short": 2.5,       # 简答题 - 较高
    "code": 3.5,        # 编程题 - 最高
}


@dataclass
class PaperSpec:
    course_id: int
    knowledge_points: List[str]
    target_difficulty: float
    question_type_counts: Dict[str, int]
    total_score: int = 100
    chapter_ids: Optional[List[int]] = None


@dataclass
class QuestionAssignment:
    question: Question
    assigned_score: float


@dataclass
class AssemblyResult:
    assignments: List[QuestionAssignment]
    unmet_requirements: Dict[str, int]
    missing_knowledge_points: List[str]
    average_difficulty: float

    def is_complete(self) -> bool:
        return not self.unmet_requirements and not self.missing_knowledge_points

    def to_dict(self) -> Dict:
        return {
            "total_questions": len(self.assignments),
            "average_difficulty": self.average_difficulty,
            "missing_types": self.unmet_requirements,
            "missing_knowledge_points": self.missing_knowledge_points,
            "questions": [
                {
                    "id": assignment.question.id,
                    "type": assignment.question.question_type,
                    "difficulty": assignment.question.difficulty,
                    "knowledge_point": assignment.question.knowledge_point_display,
                    "score": assignment.assigned_score,
                    "title": assignment.question.title,
                }
                for assignment in self.assignments
            ],
        }


class PaperAssembler:
    """Select questions from the repository to match a given paper specification."""

    def __init__(self, repository: QuestionBankRepository):
        self.repository = repository

    def assemble(self, spec: PaperSpec) -> AssemblyResult:
        filters = QuestionSearchFilters(
            course_id=spec.course_id,
            chapter_ids=spec.chapter_ids,
            knowledge_points=spec.knowledge_points,
            question_types=spec.question_type_counts.keys(),
        )

        candidates = self.repository.fetch_questions(filters)
        grouped: Dict[str, List[Question]] = defaultdict(list)
        for question in candidates:
            grouped[question.question_type].append(question)

        assignments: List[QuestionAssignment] = []
        selected_ids = set()
        selected_by_type: Dict[str, int] = defaultdict(int)
        unmet: Dict[str, int] = {}

        # 第一遍：选择题目（暂不分配分数）
        for q_type, required in spec.question_type_counts.items():
            pool = grouped.get(q_type, [])
            ranked = sorted(pool, key=lambda q: self._priority(q, spec))
            if not ranked:
                unmet[q_type] = required
                continue
            for candidate in ranked:
                if candidate.id in selected_ids:
                    continue
                # 暂时分配0分，后面统一计算
                assignments.append(QuestionAssignment(candidate, assigned_score=0))
                selected_ids.add(candidate.id)
                selected_by_type[q_type] += 1
                if selected_by_type[q_type] >= required:
                    break

            if selected_by_type[q_type] < required:
                unmet[q_type] = required - selected_by_type[q_type]

        # 第二遍：智能分配分数
        self._assign_scores_intelligently(assignments, spec.total_score)

        coverage = Counter(
            assignment.question.knowledge_point_display for assignment in assignments
        )
        missing_kp = [kp for kp in spec.knowledge_points if kp not in coverage]
        average_difficulty = (
            sum(a.question.difficulty for a in assignments) / len(assignments)
            if assignments
            else 0.0
        )


        return AssemblyResult(
            assignments=assignments,
            unmet_requirements=unmet,
            missing_knowledge_points=missing_kp,
            average_difficulty=average_difficulty,
        )

    def _assign_scores_intelligently(
        self, assignments: List[QuestionAssignment], total_score: int
    ) -> None:
        """
        智能分配分数：根据题型和难度权重分配分数。
        
        分配策略：
        1. 题型权重：简答题、编程题等主观题权重高，选择题、判断题权重低
        2. 难度权重：难度越高的题目分数越高（难度1-5对应权重0.8-1.2）
        3. 最终分数 = 基础分数 * 题型权重 * 难度权重，然后按比例分配总分
        """
        if not assignments:
            return
        
        # 计算每道题的权重
        weights = []
        for assignment in assignments:
            q = assignment.question
            # 题型权重
            type_weight = QUESTION_TYPE_WEIGHTS.get(q.question_type, 1.0)
            # 难度权重：难度1-5映射到0.8-1.2
            difficulty_weight = 0.8 + (q.difficulty - 1) * 0.1
            # 综合权重
            combined_weight = type_weight * difficulty_weight
            weights.append(combined_weight)
        
        # 计算权重总和
        total_weight = sum(weights)
        
        if total_weight == 0:
            # 如果权重为0，平均分配
            avg_score = total_score / len(assignments)
            for assignment in assignments:
                assignment.assigned_score = round(avg_score, 1)
            return
        
        # 按权重比例分配分数
        raw_scores = [(w / total_weight) * total_score for w in weights]
        
        # 四舍五入到整数或0.5
        rounded_scores = [round(s * 2) / 2 for s in raw_scores]
        
        # 确保分数不为0（至少1分）
        rounded_scores = [max(1.0, s) for s in rounded_scores]
        
        # 调整总分误差
        current_total = sum(rounded_scores)
        diff = total_score - current_total
        
        if diff != 0:
            # 找到权重最大的题目进行调整
            max_weight_idx = weights.index(max(weights))
            rounded_scores[max_weight_idx] += diff
        
        # 赋值
        for i, assignment in enumerate(assignments):
            assignment.assigned_score = rounded_scores[i]

    @staticmethod
    def _priority(question: Question, spec: PaperSpec) -> float:
        difficulty_gap = abs(question.difficulty - spec.target_difficulty)
        kp = question.knowledge_point_display
        kp_penalty = 0.0 if kp in spec.knowledge_points else 0.4
        return difficulty_gap + kp_penalty
