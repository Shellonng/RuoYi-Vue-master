package com.exampl.smartcourse.mapper.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.AssignmentSubmission;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AssignmentSubmissionMapper {

    @Select("SELECT id, assignment_id AS assignmentId, student_user_id AS studentUserId, status, score, feedback, submit_time AS submitTime, grade_time AS gradeTime, graded_by AS gradedBy, content, create_time AS createTime, update_time AS updateTime, file_name AS fileName, file_path AS filePath, is_deleted AS isDeleted, delete_time AS deleteTime FROM assignment_submission WHERE id = #{id}")
    AssignmentSubmission selectById(@Param("id") Long id);

    @Select("SELECT id, assignment_id AS assignmentId, student_user_id AS studentUserId, status, score, feedback, submit_time AS submitTime, grade_time AS gradeTime, graded_by AS gradedBy, content, create_time AS createTime, update_time AS updateTime, file_name AS fileName, file_path AS filePath, is_deleted AS isDeleted, delete_time AS deleteTime FROM assignment_submission WHERE assignment_id = #{assignmentId}")
    List<AssignmentSubmission> selectByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Select("SELECT id, assignment_id AS assignmentId, student_user_id AS studentUserId, status, score, feedback, submit_time AS submitTime, grade_time AS gradeTime, graded_by AS gradedBy, content, create_time AS createTime, update_time AS updateTime, file_name AS fileName, file_path AS filePath, is_deleted AS isDeleted, delete_time AS deleteTime FROM assignment_submission WHERE student_user_id = #{studentUserId}")
    List<AssignmentSubmission> selectByStudentUserId(@Param("studentUserId") Long studentUserId);

    @Select("SELECT id, assignment_id AS assignmentId, student_user_id AS studentUserId, status, score, feedback, submit_time AS submitTime, grade_time AS gradeTime, graded_by AS gradedBy, content, create_time AS createTime, update_time AS updateTime, file_name AS fileName, file_path AS filePath, is_deleted AS isDeleted, delete_time AS deleteTime FROM assignment_submission WHERE assignment_id = #{assignmentId} AND status = 1")
    List<AssignmentSubmission> selectPendingByAssignment(@Param("assignmentId") Long assignmentId);

    @Insert("INSERT INTO assignment_submission(assignment_id, student_user_id, status, score, feedback, submit_time, grade_time, graded_by, content, create_time, update_time, file_name, file_path, is_deleted, delete_time) " +
            "VALUES(#{assignmentId}, #{studentUserId}, #{status}, #{score}, #{feedback}, #{submitTime}, #{gradeTime}, #{gradedBy}, #{content}, #{createTime}, #{updateTime}, #{fileName}, #{filePath}, #{isDeleted}, #{deleteTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AssignmentSubmission submission);

    @Update("UPDATE assignment_submission SET assignment_id=#{assignmentId}, student_user_id=#{studentUserId}, status=#{status}, score=#{score}, feedback=#{feedback}, submit_time=#{submitTime}, grade_time=#{gradeTime}, graded_by=#{gradedBy}, content=#{content}, create_time=#{createTime}, update_time=#{updateTime}, file_name=#{fileName}, file_path=#{filePath}, is_deleted=#{isDeleted}, delete_time=#{deleteTime} WHERE id=#{id}")
    int update(AssignmentSubmission submission);

    @Delete("DELETE FROM assignment_submission WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT s.id, s.assignment_id AS assignmentId, s.student_user_id AS studentUserId, s.status, s.score, s.feedback, s.submit_time AS submitTime, s.grade_time AS gradeTime, s.graded_by AS gradedBy, s.content, s.create_time AS createTime, s.update_time AS updateTime, s.file_name AS fileName, s.file_path AS filePath, s.is_deleted AS isDeleted, s.delete_time AS deleteTime FROM assignment_submission s JOIN assignment a ON a.id = s.assignment_id WHERE s.status = 1 AND a.mode = 'file'")
    List<AssignmentSubmission> selectPendingFileMode();

    @Select("SELECT COUNT(*) FROM assignment_submission")
    long countAll();

    @Select("<script>SELECT id, assignment_id AS assignmentId, student_user_id AS studentUserId, status, score, feedback, submit_time AS submitTime, grade_time AS gradeTime, graded_by AS gradedBy, content, create_time AS createTime, update_time AS updateTime, file_name AS fileName, file_path AS filePath, is_deleted AS isDeleted, delete_time AS deleteTime FROM assignment_submission ORDER BY ${orderClause} LIMIT #{limit} OFFSET #{offset}</script>")
    List<AssignmentSubmission> selectPaged(@Param("orderClause") String orderClause, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>SELECT s.id, s.assignment_id AS assignmentId, s.student_user_id AS studentUserId, s.status, s.score, s.feedback, s.submit_time AS submitTime, s.grade_time AS gradeTime, s.graded_by AS gradedBy, s.content, s.create_time AS createTime, s.update_time AS updateTime, s.file_name AS fileName, s.file_path AS filePath, s.is_deleted AS isDeleted, s.delete_time AS deleteTime FROM assignment_submission s JOIN `user` u ON u.id = s.student_user_id ORDER BY u.real_name ${order} LIMIT #{limit} OFFSET #{offset}</script>")
    List<AssignmentSubmission> selectPagedOrderByRealName(@Param("order") String order, @Param("offset") int offset, @Param("limit") int limit);

    @Update("UPDATE assignment_submission s JOIN (SELECT g.assignment_submission_id AS sid, g.total_score AS ts FROM ai_grading g JOIN (SELECT assignment_submission_id AS sid, MAX(created_at) AS mc FROM ai_grading GROUP BY assignment_submission_id) t ON g.assignment_submission_id = t.sid AND g.created_at = t.mc) lg ON lg.sid = s.id SET s.status = 2, s.score = CAST(ROUND(lg.ts) AS SIGNED)")
    int markAllWithGradingAsGraded();
}
