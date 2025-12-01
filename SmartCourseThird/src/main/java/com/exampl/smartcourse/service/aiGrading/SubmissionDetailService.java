package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.SubmissionDetailResponse;
import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.entity.aiGrading.AssignmentSubmission;
import com.exampl.smartcourse.entity.aiGrading.KnowledgePoint;
import com.exampl.smartcourse.entity.aiGrading.Student;
import com.exampl.smartcourse.entity.aiGrading.User;
import com.exampl.smartcourse.mapper.aiGrading.AiGradingMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentSubmissionMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentKpMapper;
import com.exampl.smartcourse.mapper.aiGrading.KnowledgePointMapper;
import com.exampl.smartcourse.mapper.aiGrading.StudentMapper;
import com.exampl.smartcourse.mapper.aiGrading.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionDetailService {

    private final AssignmentSubmissionMapper submissionMapper;
    private final AssignmentMapper assignmentMapper;
    private final StudentMapper studentMapper;
    private final AiGradingMapper aiGradingMapper;
    private final AssignmentKpMapper assignmentKpMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final UserMapper userMapper;

    public SubmissionDetailResponse getDetail(Long submissionId) {
        AssignmentSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return null;
        }
        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        Student student = studentMapper.selectByUserId(submission.getStudentUserId());
        User user = userMapper.selectById(submission.getStudentUserId());
        AiGrading grading = aiGradingMapper.selectLatestBySubmissionId(submissionId);
        List<Long> kpIds = assignmentKpMapper.selectByAssignmentId(submission.getAssignmentId())
                .stream().map(a -> a.getKpId()).collect(Collectors.toList());
        List<KnowledgePoint> kps = kpIds.stream()
                .map(knowledgePointMapper::selectById)
                .filter(k -> k != null)
                .collect(Collectors.toList());

        SubmissionDetailResponse resp = new SubmissionDetailResponse();
        resp.setSubmission(submission);
        resp.setAssignment(assignment);
        resp.setStudent(student);
        resp.setUser(user);
        if (user != null) {
            resp.setUserId(user.getId());
            resp.setRealName(user.getRealName());
        } else {
            resp.setUserId(submission.getStudentUserId());
        }
        resp.setGrading(grading);
        resp.setKnowledgePoints(kps);
        return resp;
    }
}
