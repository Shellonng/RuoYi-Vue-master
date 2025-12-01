package com.exampl.smartcourse.mapper;

import com.exampl.smartcourse.dto.StudentBehaviorAggregate;
import com.exampl.smartcourse.entity.VideoLearningBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoLearningBehaviorMapper {

    @Select("""
        <script>
        SELECT *
        FROM video_learning_behavior
        <where>
            <if test="videoId != null">
                AND video_id = #{videoId}
            </if>
            <if test="studentId != null">
                AND student_id = #{studentId}
            </if>
        </where>
        </script>
        """)
    List<VideoLearningBehavior> selectByVideo(@Param("videoId") Long videoId,
                                              @Param("studentId") Long studentId);

    @Select("""
        <script>
        SELECT vlb.*
        FROM video_learning_behavior vlb
        JOIN video v ON vlb.video_id = v.id
        <where>
            <if test="courseId != null">
                AND v.course_id = #{courseId}
            </if>
            <if test="videoId != null">
                AND vlb.video_id = #{videoId}
            </if>
            <if test="studentId != null">
                AND vlb.student_id = #{studentId}
            </if>
        </where>
        </script>
        """)
    List<VideoLearningBehavior> selectByCourseAndVideo(@Param("courseId") Long courseId,
                                                       @Param("videoId") Long videoId,
                                                       @Param("studentId") Long studentId);

    @Select("""
        <script>
        SELECT
            vlb.student_id,
            v.course_id,
            AVG(vlb.completion_rate) AS avg_completion_rate,
            SUM(vlb.watch_duration) AS total_watch_duration,
            SUM(vlb.watch_count) AS total_watch_count
        FROM video_learning_behavior vlb
        JOIN video v ON vlb.video_id = v.id
        <where>
            <if test="courseId != null">
                AND v.course_id = #{courseId}
            </if>
            <if test="studentId != null">
                AND vlb.student_id = #{studentId}
            </if>
        </where>
        GROUP BY vlb.student_id, v.course_id
        </script>
        """)
    List<StudentBehaviorAggregate> aggregateBehavior(@Param("courseId") Long courseId,
                                                     @Param("studentId") Long studentId);
}
