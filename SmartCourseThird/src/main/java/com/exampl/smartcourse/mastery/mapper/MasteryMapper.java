package com.exampl.smartcourse.mastery.mapper;

import com.exampl.smartcourse.mastery.model.StudentKPMasteryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MasteryMapper {

    @Select("""
            <script>
            SELECT
                m.kp_id AS kpId,
                kp.title AS kpTitle,
                m.student_user_id AS studentUserId,
                m.mastery_score AS masteryScore,
                m.mastery_status AS masteryStatus,
                m.trend AS trend,
                m.update_time AS updateTime
            FROM student_kp_mastery m
                     JOIN knowledge_point kp ON kp.id = m.kp_id
            WHERE m.is_deleted = 0
            <if test="courseId != null">
                AND m.course_id = #{courseId}
            </if>
            <if test="studentUserId != null">
                AND m.student_user_id = #{studentUserId}
            </if>
            </script>
            """)
    List<StudentKPMasteryDO> listStudentMastery(@Param("courseId") Long courseId,
                                                @Param("studentUserId") Long studentUserId);

    @Select("""
            <script>
            SELECT
                m.kp_id AS kpId,
                kp.title AS kpTitle,
                m.student_user_id AS studentUserId,
                m.mastery_score AS masteryScore,
                m.mastery_status AS masteryStatus,
                m.trend AS trend,
                m.update_time AS updateTime
            FROM student_kp_mastery m
                     JOIN knowledge_point kp ON kp.id = m.kp_id
                     JOIN class_student cs ON cs.student_user_id = m.student_user_id
            WHERE m.is_deleted = 0
              AND cs.class_id = #{classId}
            <if test="courseId != null">
                AND m.course_id = #{courseId}
            </if>
            </script>
            """)
    List<StudentKPMasteryDO> listClassMastery(@Param("courseId") Long courseId,
                                             @Param("classId") Long classId);
}
