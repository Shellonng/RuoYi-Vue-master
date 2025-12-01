package com.exampl.smartcourse.mapper.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.KnowledgePoint;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface KnowledgePointMapper {

    @Select("SELECT id, course_id AS courseId, title, description, level, creator_user_id AS creatorUserId, create_time AS createTime, update_time AS updateTime, is_deleted AS isDeleted, delete_time AS deleteTime FROM knowledge_point WHERE id = #{id}")
    KnowledgePoint selectById(@Param("id") Long id);

    @Insert("INSERT INTO knowledge_point(course_id, title, description, level, creator_user_id, create_time, update_time, is_deleted, delete_time) VALUES(#{courseId}, #{title}, #{description}, #{level}, #{creatorUserId}, #{createTime}, #{updateTime}, #{isDeleted}, #{deleteTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgePoint kp);

    @Update("UPDATE knowledge_point SET course_id=#{courseId}, title=#{title}, description=#{description}, level=#{level}, creator_user_id=#{creatorUserId}, create_time=#{createTime}, update_time=#{updateTime}, is_deleted=#{isDeleted}, delete_time=#{deleteTime} WHERE id=#{id}")
    int update(KnowledgePoint kp);

    @Delete("DELETE FROM knowledge_point WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, course_id AS courseId, title, description, level, creator_user_id AS creatorUserId, create_time AS createTime, update_time AS updateTime, is_deleted AS isDeleted, delete_time AS deleteTime FROM knowledge_point WHERE course_id = #{courseId} AND is_deleted = 0")
    List<KnowledgePoint> selectByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT id, course_id AS courseId, title, description, level, creator_user_id AS creatorUserId, create_time AS createTime, update_time AS updateTime, is_deleted AS isDeleted, delete_time AS deleteTime FROM knowledge_point WHERE creator_user_id = #{creatorUserId} AND is_deleted = 0")
    List<KnowledgePoint> selectByCreator(@Param("creatorUserId") Long creatorUserId);
}