from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Optional

from assembly import AssemblyResult, PaperSpec
from config import DBConfig
from db import create_connection


@dataclass
class PublishRequest:
    title: str
    publisher_user_id: int
    assignment_type: str  # homework | exam
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None
    description: Optional[str] = None
    mode: str = "question"
    time_limit: Optional[int] = None  # 时间限制（分钟），仅考试类型有效


class AssignmentPublisher:
    """Create assignment records directly in the SmartCourse database."""

    def __init__(self, config: DBConfig):
        self.config = config

    def publish(self, spec: PaperSpec, result: AssemblyResult, request: PublishRequest) -> int:
        """Persist the generated paper as an assignment and return its ID."""
        with create_connection(self.config) as conn:
            with conn.cursor() as cursor:
                # 如果是考试类型且有时间限制，使用时间限制；否则为 NULL
                time_limit = request.time_limit if request.assignment_type == 'exam' else None
                
                cursor.execute(
                    """
                    INSERT INTO assignment
                    (title, course_id, publisher_user_id, type, description,
                     start_time, end_time, status, mode, total_score, time_limit)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    """,
                    (
                        request.title,
                        spec.course_id,
                        request.publisher_user_id,
                        request.assignment_type,
                        request.description,
                        request.start_time,
                        request.end_time,
                        1,  # published
                        request.mode,
                        spec.total_score,
                        time_limit,
                    ),
                )
                assignment_id = cursor.lastrowid

                for seq, assignment in enumerate(result.assignments, start=1):
                    cursor.execute(
                        """
                        INSERT INTO assignment_question (assignment_id, question_id, score, sequence)
                        VALUES (%s, %s, %s, %s)
                        """,
                        (
                            assignment_id,
                            assignment.question.id,
                            round(assignment.assigned_score),
                            seq,
                        ),
                    )

                kp_ids = [
                    self._try_extract_kp_id(a.question.knowledge_point_raw)
                    for a in result.assignments
                ]
                kp_ids = [kp_id for kp_id in kp_ids if kp_id is not None]
                for seq, kp_id in enumerate(dict.fromkeys(kp_ids), start=1):
                    cursor.execute(
                        """
                        INSERT IGNORE INTO assignment_kp (assignment_id, kp_id, sequence, is_required)
                        VALUES (%s, %s, %s, %s)
                        """,
                        (assignment_id, kp_id, seq, 1),
                    )

        return assignment_id

    @staticmethod
    def _try_extract_kp_id(raw_value: Optional[str]) -> Optional[int]:
        if raw_value is None:
            return None
        try:
            return int(str(raw_value))
        except ValueError:
            return None
