package com.exampl.smartcourse.mapper.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.Student;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StudentMapper {

    @Select("SELECT id, user_id AS userId, enrollment_status AS enrollmentStatus, gpa, gpa_level AS gpaLevel, create_time AS createTime, update_time AS updateTime, is_deleted AS isDeleted, delete_time AS deleteTime FROM student WHERE id = #{id}")
    Student selectById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, enrollment_status AS enrollmentStatus, gpa, gpa_level AS gpaLevel, create_time AS createTime, update_time AS updateTime, is_deleted AS isDeleted, delete_time AS deleteTime FROM student WHERE user_id = #{userId}")
    Student selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO student(user_id, enrollment_status, gpa, gpa_level, create_time, update_time, is_deleted, delete_time) " +
            "VALUES(#{userId}, #{enrollmentStatus}, #{gpa}, #{gpaLevel}, #{createTime}, #{updateTime}, #{isDeleted}, #{deleteTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);

    @Update("UPDATE student SET user_id=#{userId}, enrollment_status=#{enrollmentStatus}, gpa=#{gpa}, gpa_level=#{gpaLevel}, create_time=#{createTime}, update_time=#{updateTime}, is_deleted=#{isDeleted}, delete_time=#{deleteTime} WHERE id=#{id}")
    int update(Student student);

    @Delete("DELETE FROM student WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
