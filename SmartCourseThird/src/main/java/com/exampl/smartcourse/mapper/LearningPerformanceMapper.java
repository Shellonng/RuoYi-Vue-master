package com.exampl.smartcourse.mapper;

import com.exampl.smartcourse.dto.BehaviorPerformanceRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LearningPerformanceMapper {

    @Select("""
        <script>
        WITH behavior AS (
            SELECT
                vlb.student_id AS student_id,
                v.course_id AS course_id,
                AVG(vlb.completion_rate) AS avg_completion_rate,
                SUM(vlb.watch_duration) AS total_watch_duration,
                SUM(vlb.watch_count) AS total_watch_count
            FROM video_learning_behavior vlb
            JOIN video v ON vlb.video_id = v.id
            <where>
                <if test="videoId != null">
                    AND vlb.video_id = #{videoId}
                </if>
            </where>
            GROUP BY vlb.student_id, v.course_id
        )
        SELECT
            lpr.student_id,
            lpr.course_id,
            lpr.avg_score,
            lpr.exam_count,
            lpr.homework_count,
            IFNULL(b.avg_completion_rate, 0) AS avg_completion_rate,
            IFNULL(b.total_watch_duration, 0) AS total_watch_duration,
            IFNULL(b.total_watch_count, 0) AS total_watch_count
        FROM learning_performance_relevance lpr
        LEFT JOIN behavior b ON lpr.student_id = b.student_id AND lpr.course_id = b.course_id
        <where>
            <if test="videoId != null">
                AND b.student_id IS NOT NULL
            </if>
            <if test="courseId != null">
                AND lpr.course_id = #{courseId}
            </if>
        </where>
        </script>
        """)
    List<BehaviorPerformanceRow> loadBehaviorPerformance(@Param("courseId") Long courseId, @Param("videoId") Long videoId);
}

