package com.exampl.smartcourse.mapper.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.Assignment;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Mapper
@Component("aiGradingAssignmentMapper")
public interface AssignmentMapper {

    @Select("SELECT id, title, course_id AS courseId, publisher_user_id AS publisherUserId, type, description, start_time AS startTime, end_time AS endTime, create_time AS createTime, status, update_time AS updateTime, mode, time_limit AS timeLimit, total_score AS totalScore, duration, allowed_file_types AS allowedFileTypes, attachments AS attachments, is_deleted AS isDeleted, delete_time AS deleteTime FROM assignment WHERE id = #{id}")
    Assignment selectById(@Param("id") Long id);

    @Select("SELECT id, title, course_id AS courseId, publisher_user_id AS publisherUserId, type, description, start_time AS startTime, end_time AS endTime, create_time AS createTime, status, update_time AS updateTime, mode, time_limit AS timeLimit, total_score AS totalScore, duration, allowed_file_types AS allowedFileTypes, attachments AS attachments, is_deleted AS isDeleted, delete_time AS deleteTime FROM assignment")
    List<Assignment> selectAll();

    @Select("SELECT COUNT(*) FROM assignment")
    long countAll();

    @Select("<script>SELECT id, title, course_id AS courseId, publisher_user_id AS publisherUserId, type, description, start_time AS startTime, end_time AS endTime, create_time AS createTime, status, update_time AS updateTime, mode, time_limit AS timeLimit, total_score AS totalScore, duration, allowed_file_types AS allowedFileTypes, attachments AS attachments, is_deleted AS isDeleted, delete_time AS deleteTime FROM assignment ORDER BY ${orderClause} LIMIT #{limit} OFFSET #{offset}</script>")
    List<Assignment> selectPaged(@Param("orderClause") String orderClause, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>SELECT a.id, a.title, a.course_id AS courseId, a.publisher_user_id AS publisherUserId, a.type, a.description, a.start_time AS startTime, a.end_time AS endTime, a.create_time AS createTime, a.status, a.update_time AS updateTime, a.mode, a.time_limit AS timeLimit, a.total_score AS totalScore, a.duration, a.allowed_file_types AS allowedFileTypes, a.attachments AS attachments, a.is_deleted AS isDeleted, a.delete_time AS deleteTime FROM assignment a LEFT JOIN `user` u ON u.id = a.publisher_user_id ORDER BY u.real_name ${order} LIMIT #{limit} OFFSET #{offset}</script>")
    List<Assignment> selectPagedOrderByPublisherRealName(@Param("order") String order, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>SELECT DISTINCT a.id, a.title, a.course_id AS courseId, a.publisher_user_id AS publisherUserId, a.type, a.description, a.start_time AS startTime, a.end_time AS endTime, a.create_time AS createTime, a.status, a.update_time AS updateTime, a.mode, a.time_limit AS timeLimit, a.total_score AS totalScore, a.duration, a.allowed_file_types AS allowedFileTypes, a.attachments AS attachments, a.is_deleted AS isDeleted, a.delete_time AS deleteTime FROM assignment a JOIN assignment_submission s ON s.assignment_id = a.id WHERE s.student_user_id = #{studentUserId} ORDER BY ${orderClause} LIMIT #{limit} OFFSET #{offset}</script>")
    List<Assignment> selectByStudentUserIdPaged(@Param("studentUserId") Long studentUserId, @Param("orderClause") String orderClause, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(DISTINCT a.id) FROM assignment a JOIN assignment_submission s ON s.assignment_id = a.id WHERE s.student_user_id = #{studentUserId}")
    long countByStudentUserId(@Param("studentUserId") Long studentUserId);

    @Select("<script>SELECT DISTINCT a.id, a.title, a.course_id AS courseId, a.publisher_user_id AS publisherUserId, a.type, a.description, a.start_time AS startTime, a.end_time AS EndTime, a.create_time AS createTime, a.status, a.update_time AS updateTime, a.mode, a.time_limit AS timeLimit, a.total_score AS totalScore, a.duration, a.allowed_file_types AS allowedFileTypes, a.attachments AS attachments, a.is_deleted AS isDeleted, a.delete_time AS deleteTime FROM assignment a JOIN assignment_submission s ON s.assignment_id = a.id LEFT JOIN `user` u ON u.id = a.publisher_user_id WHERE s.student_user_id = #{studentUserId} ORDER BY u.real_name ${order} LIMIT #{limit} OFFSET #{offset}</script>")
    List<Assignment> selectByStudentUserIdOrderByPublisherRealName(@Param("studentUserId") Long studentUserId, @Param("order") String order, @Param("offset") int offset, @Param("limit") int limit);

    @Insert("INSERT INTO assignment(title, course_id, publisher_user_id, type, description, start_time, end_time, create_time, status, update_time, mode, time_limit, total_score, duration, allowed_file_types, attachments, is_deleted, delete_time) " +
            "VALUES(#{title}, #{courseId}, #{publisherUserId}, #{type}, #{description}, #{startTime}, #{endTime}, #{createTime}, #{status}, #{updateTime}, #{mode}, #{timeLimit}, #{totalScore}, #{duration}, #{allowedFileTypes}, #{attachments}, #{isDeleted}, #{deleteTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Assignment assignment);

    @Update("UPDATE assignment SET title=#{title}, course_id=#{courseId}, publisher_user_id=#{publisherUserId}, type=#{type}, description=#{description}, start_time=#{startTime}, end_time=#{endTime}, create_time=#{createTime}, status=#{status}, update_time=#{updateTime}, mode=#{mode}, time_limit=#{timeLimit}, total_score=#{totalScore}, duration=#{duration}, allowed_file_types=#{allowedFileTypes}, attachments=#{attachments}, is_deleted=#{isDeleted}, delete_time=#{deleteTime} WHERE id=#{id}")
    int update(Assignment assignment);

    @Delete("DELETE FROM assignment WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
