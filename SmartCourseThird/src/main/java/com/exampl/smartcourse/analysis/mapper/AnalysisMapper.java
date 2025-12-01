package com.exampl.smartcourse.analysis.mapper;

import com.exampl.smartcourse.analysis.model.KpScoreAggDO;
import com.exampl.smartcourse.analysis.model.ScoreTrendDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnalysisMapper {

    /**
     * 学生在知识点上的平均得分比例。
     */
    @Select("""
            <script>
            SELECT
                CAST(q.knowledge_point AS SIGNED) AS kpId,
                kp.title AS kpTitle,
                SUM(sa.score) / NULLIF(SUM(aq.score), 0) AS scoreRatio
            FROM student_answer sa
                     JOIN assignment_question aq ON aq.question_id = sa.question_id
                        AND aq.assignment_id = sa.assignment_id
                     JOIN assignment a ON a.id = aq.assignment_id
                     JOIN question q ON q.id = aq.question_id
                     LEFT JOIN knowledge_point kp ON kp.id = CAST(q.knowledge_point AS SIGNED)
            WHERE sa.student_user_id = #{studentUserId}
              AND sa.score IS NOT NULL
            <if test="courseId != null">
              AND a.course_id = #{courseId}
            </if>
            GROUP BY CAST(q.knowledge_point AS SIGNED), kp.title
            </script>
            """)
    List<KpScoreAggDO> listStudentKpScores(@Param("courseId") Long courseId,
                                           @Param("studentUserId") Long studentUserId);

    /**
     * 学生成绩趋势。
     */
    @Select("""
            <script>
            SELECT
                UNIX_TIMESTAMP(COALESCE(s.submit_time, a.end_time, a.start_time)) * 1000 AS timestamp,
                s.score / NULLIF(a.total_score, 0) AS scoreRatio
            FROM assignment_submission s
                     JOIN assignment a ON a.id = s.assignment_id
            WHERE s.student_user_id = #{studentUserId}
            <if test="courseId != null">
              AND a.course_id = #{courseId}
            </if>
            ORDER BY COALESCE(s.submit_time, a.end_time, a.start_time) ASC
            </script>
            """)
    List<ScoreTrendDO> listScoreTrend(@Param("courseId") Long courseId,
                                      @Param("studentUserId") Long studentUserId);
}
