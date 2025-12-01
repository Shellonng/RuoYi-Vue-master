package com.exampl.smartcourse.mapper.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.CourseResource;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CourseResourceMapper {

    @Select("SELECT id, course_id AS courseId, name, file_type AS fileType, file_size AS fileSize, file_url AS fileUrl, description, download_count AS downloadCount, upload_user_id AS uploadUserId, create_time AS createTime, update_time AS updateTime FROM course_resource WHERE id = #{id}")
    CourseResource selectById(@Param("id") Long id);

    @Select("SELECT id, course_id AS courseId, name, file_type AS fileType, file_size AS fileSize, file_url AS fileUrl, description, download_count AS downloadCount, upload_user_id AS uploadUserId, create_time AS createTime, update_time AS updateTime FROM course_resource WHERE course_id = #{courseId}")
    List<CourseResource> selectByCourseId(@Param("courseId") Long courseId);

    @Insert("INSERT INTO course_resource(course_id, name, file_type, file_size, file_url, description, download_count, upload_user_id, create_time, update_time) VALUES(#{courseId}, #{name}, #{fileType}, #{fileSize}, #{fileUrl}, #{description}, #{downloadCount}, #{uploadUserId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CourseResource resource);

    @Update("UPDATE course_resource SET course_id=#{courseId}, name=#{name}, file_type=#{fileType}, file_size=#{fileSize}, file_url=#{fileUrl}, description=#{description}, download_count=#{downloadCount}, upload_user_id=#{uploadUserId}, create_time=#{createTime}, update_time=#{updateTime} WHERE id=#{id}")
    int update(CourseResource resource);

    @Delete("DELETE FROM course_resource WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}