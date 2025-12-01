package com.exampl.smartcourse.mapper;

import com.exampl.smartcourse.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("SELECT id, title, description, cover_image AS coverImage, term, status, teacher_user_id AS teacherUserId, student_count AS studentCount FROM course WHERE is_deleted = 0 AND teacher_user_id = #{teacherUserId}")
    List<Course> selectByTeacher(@Param("teacherUserId") Long teacherUserId);

    @Select("SELECT c.id, c.title, c.description, c.cover_image AS coverImage, c.term, c.status, c.teacher_user_id AS teacherUserId, c.student_count AS studentCount FROM course c INNER JOIN course_student cs ON cs.course_id = c.id WHERE cs.student_user_id = #{studentUserId} AND c.is_deleted = 0")
    List<Course> selectByStudent(@Param("studentUserId") Long studentUserId);
}
