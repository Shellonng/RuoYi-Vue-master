package com.exampl.smartcourse.mapper;

import com.exampl.smartcourse.dto.StudentKpMasteryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentKpMasteryMapper {

    @Select("""
        <script>
        SELECT
            skm.student_user_id AS student_id,
            skm.course_id,
            skm.kp_id,
            kp.title AS kp_title,
            kp.level AS kp_level,
            skm.mastery_score,
            skm.mastery_status
        FROM student_kp_mastery skm
        LEFT JOIN knowledge_point kp ON skm.kp_id = kp.id
        <where>
            skm.is_deleted = 0
            <if test="courseId != null">
                AND skm.course_id = #{courseId}
            </if>
            <if test="studentId != null">
                AND skm.student_user_id = #{studentId}
            </if>
            <if test="kpId != null">
                AND skm.kp_id = #{kpId}
            </if>
        </where>
        </script>
        """)
    List<StudentKpMasteryRecord> selectMastery(@Param("courseId") Long courseId,
                                               @Param("studentId") Long studentId,
                                               @Param("kpId") Long kpId);
}

