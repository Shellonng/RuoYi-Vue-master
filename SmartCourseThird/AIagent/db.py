from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Iterable, List, Optional

from config import DBConfig

try:
    import pymysql
except ImportError:  # pragma: no cover - dependency hint
    pymysql = None


QUESTION_TYPES = ["single", "multiple", "true_false", "blank", "short", "code"]


@dataclass
class KnowledgePoint:
    id: int
    title: str
    level: str
    description: Optional[str] = None


@dataclass
class Question:
    id: int
    title: str
    question_type: str
    difficulty: int
    knowledge_point_raw: Optional[str]
    knowledge_point_title: Optional[str]
    course_id: int
    chapter_id: int
    correct_answer: Optional[str]
    explanation: Optional[str]

    @property
    def knowledge_point_display(self) -> str:
        return self.knowledge_point_title or self.knowledge_point_raw or "未标注"


@dataclass
class QuestionSearchFilters:
    course_id: Optional[int] = None
    chapter_ids: Optional[Iterable[int]] = None
    knowledge_points: Optional[Iterable[str]] = None
    question_types: Optional[Iterable[str]] = None
    limit: Optional[int] = None


def create_connection(config: DBConfig):
    """Create a pymysql connection using the repository configuration."""
    if pymysql is None:  # pragma: no cover - import guidance
        raise RuntimeError(
            "pymysql is required for database access. Install it via `pip install pymysql`."
        )
    return pymysql.connect(
        host=config.host,
        port=config.port,
        user=config.user,
        password=config.password,
        db=config.database,
        charset=config.charset,
        cursorclass=pymysql.cursors.DictCursor,
        autocommit=True,
    )


class QuestionBankRepository:
    """Lightweight repository over the SmartCourse MySQL schema."""

    def __init__(self, config: DBConfig):
        self.config = config

    def _connect(self):
        return create_connection(self.config)

    def list_knowledge_points(self, course_id: Optional[int] = None) -> List[KnowledgePoint]:
        sql = (
            "SELECT id, title, level, description "
            "FROM knowledge_point "
            "WHERE is_deleted = 0"
        )
        params: List = []
        if course_id:
            sql += " AND course_id = %s"
            params.append(course_id)
        sql += " ORDER BY title"

        with self._connect() as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, params)
                rows = cursor.fetchall()

        return [
            KnowledgePoint(
                id=row["id"],
                title=row["title"],
                level=row["level"],
                description=row.get("description"),
            )
            for row in rows
        ]

    def summarize_question_types(self, course_id: Optional[int] = None) -> Dict[str, int]:
        sql = "SELECT question_type, COUNT(*) AS total FROM question WHERE is_deleted = 0"
        params: List = []
        if course_id:
            sql += " AND course_id = %s"
            params.append(course_id)
        sql += " GROUP BY question_type"
        summary: Dict[str, int] = {q_type: 0 for q_type in QUESTION_TYPES}

        with self._connect() as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, params)
                for row in cursor.fetchall():
                    summary[row["question_type"]] = row["total"]
        return summary

    def fetch_questions(self, filters: QuestionSearchFilters) -> List[Question]:
        clauses = ["q.is_deleted = 0"]
        params: List = []
        if filters.course_id:
            clauses.append("q.course_id = %s")
            params.append(filters.course_id)
        if filters.chapter_ids:
            chapter_ids = list(filters.chapter_ids)
            placeholders = ", ".join(["%s"] * len(chapter_ids))
            clauses.append(f"q.chapter_id IN ({placeholders})")
            params.extend(chapter_ids)
        if filters.question_types:
            question_types = list(filters.question_types)
            placeholders = ", ".join(["%s"] * len(question_types))
            clauses.append(f"q.question_type IN ({placeholders})")
            params.extend(question_types)
        if filters.knowledge_points:
            kp_values = list(filters.knowledge_points)
            placeholders = ", ".join(["%s"] * len(kp_values))
            clauses.append(
                f"(COALESCE(kp.title, q.knowledge_point) IN ({placeholders}) "
                f"OR CAST(kp.id AS CHAR) IN ({placeholders}))"
            )
            params.extend(kp_values)
            params.extend(kp_values)

        sql = (
            "SELECT q.id, q.title, q.question_type, q.difficulty, q.knowledge_point, "
            "q.course_id, q.chapter_id, q.correct_answer, q.explanation, "
            "kp.title AS kp_title "
            "FROM question q "
            "LEFT JOIN knowledge_point kp "
            "ON (q.knowledge_point = kp.title OR q.knowledge_point = CAST(kp.id AS CHAR)) "
        )

        if clauses:
            sql += " WHERE " + " AND ".join(clauses)
        sql += " ORDER BY q.update_time DESC"
        if filters.limit:
            sql += " LIMIT %s"
            params.append(filters.limit)

        with self._connect() as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, params)
                rows = cursor.fetchall()

        return [
            Question(
                id=row["id"],
                title=row["title"],
                question_type=row["question_type"],
                difficulty=row["difficulty"],
                knowledge_point_raw=row["knowledge_point"],
                knowledge_point_title=row["kp_title"],
                course_id=row["course_id"],
                chapter_id=row["chapter_id"],
                correct_answer=row.get("correct_answer"),
                explanation=row.get("explanation"),
            )
            for row in rows
        ]
