package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.SubmissionDetailResponse;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.entity.aiGrading.AssignmentSubmission;
import com.exampl.smartcourse.entity.aiGrading.KnowledgePoint;
import com.exampl.smartcourse.entity.aiGrading.Student;
import com.exampl.smartcourse.mapper.aiGrading.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PendingGradingService {

    private final AssignmentSubmissionMapper assignmentSubmissionMapper;
    private final AssignmentMapper assignmentMapper;
    private final StudentMapper studentMapper;
    private final AssignmentKpMapper assignmentKpMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    public List<SubmissionDetailResponse> listPendingSubmissions(Long courseId, Long assignmentId, Long studentUserId, String keyword) {
        List<AssignmentSubmission> submissions = assignmentSubmissionMapper.selectPendingFileMode();
        return submissions.stream()
                .filter(s -> {
                    Assignment a = assignmentMapper.selectById(s.getAssignmentId());
                    if (a == null) {
                        return false;
                    }
                    
                    // 检查课程权限：如果提供了courseId参数，则只显示该课程的作业
                    if (courseId != null && !Objects.equals(a.getCourseId(), courseId)) {
                        return false;
                    }
                    
                    
                    if (assignmentId != null && !Objects.equals(s.getAssignmentId(), assignmentId)) {
                        return false;
                    }
                    if (studentUserId != null && !Objects.equals(s.getStudentUserId(), studentUserId)) {
                        return false;
                    }
                    if (keyword != null && !keyword.isEmpty()) {
                        String title = a.getTitle();
                        if (title == null || !title.toLowerCase().contains(keyword.toLowerCase())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(s -> {
                    SubmissionDetailResponse r = new SubmissionDetailResponse();
                    r.setSubmission(s);
                    r.setAssignment(assignmentMapper.selectById(s.getAssignmentId()));
                    r.setStudent(studentMapper.selectByUserId(s.getStudentUserId()));
                    r.setGrading(null);
                    List<Long> kpIds = assignmentKpMapper.selectByAssignmentId(s.getAssignmentId())
                            .stream().map(a -> a.getKpId()).collect(Collectors.toList());
                    List<KnowledgePoint> kps = kpIds.stream()
                            .map(knowledgePointMapper::selectById)
                            .filter(k -> k != null)
                            .collect(Collectors.toList());
                    r.setKnowledgePoints(kps);
                    return r;
                })
                .collect(Collectors.toList());
    }
}
