package com.exampl.smartcourse.mapper;

import com.exampl.smartcourse.entity.Video;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VideoMapper {

    @Select("SELECT * FROM video WHERE id = #{id}")
    Video findById(@Param("id") Long id);

    @Select("SELECT * FROM video WHERE course_id = #{courseId}")
    List<Video> findByCourseId(@Param("courseId") Long courseId);
}
