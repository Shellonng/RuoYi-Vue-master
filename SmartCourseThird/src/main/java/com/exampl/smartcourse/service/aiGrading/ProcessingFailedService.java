package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.SubmissionDetailResponse;
import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.entity.aiGrading.AssignmentSubmission;
import com.exampl.smartcourse.entity.aiGrading.KnowledgePoint;
import com.exampl.smartcourse.entity.aiGrading.Student;
import com.exampl.smartcourse.mapper.aiGrading.AiGradingMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentKpMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentSubmissionMapper;
import com.exampl.smartcourse.mapper.aiGrading.KnowledgePointMapper;
import com.exampl.smartcourse.mapper.aiGrading.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessingFailedService {

    private final AiGradingMapper aiGradingMapper;
    private final AssignmentSubmissionMapper submissionMapper;
    private final AssignmentMapper assignmentMapper;
    private final StudentMapper studentMapper;
    private final AssignmentKpMapper assignmentKpMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    public List<SubmissionDetailResponse> listUnresolved(Long courseId) {
        List<AiGrading> latest = aiGradingMapper.selectLatestUnresolved();
        return latest.stream()
                .map(g -> {
                    SubmissionDetailResponse r = new SubmissionDetailResponse();
                    AssignmentSubmission s = submissionMapper.selectById(g.getAssignmentSubmissionId());
                    Assignment a = s != null ? assignmentMapper.selectById(s.getAssignmentId()) : null;
                    Student st = s != null ? studentMapper.selectByUserId(s.getStudentUserId()) : null;

                    r.setSubmission(s);
                    r.setAssignment(a);
                    r.setStudent(st);
                    r.setGrading(g);

                    Long aid = s != null ? s.getAssignmentId() : null;
                    List<Long> kpIds = aid == null ? java.util.List.of()
                            : assignmentKpMapper.selectByAssignmentId(aid)
                                    .stream().map(x -> x.getKpId()).collect(Collectors.toList());
                    List<KnowledgePoint> kps = kpIds.stream()
                            .map(knowledgePointMapper::selectById)
                            .filter(k -> k != null)
                            .collect(Collectors.toList());
                    r.setKnowledgePoints(kps);
                    return r;
                })
                .filter(r -> {
                    if (courseId == null)
                        return true;
                    return r.getAssignment() != null
                            && java.util.Objects.equals(r.getAssignment().getCourseId(), courseId);
                })
                .collect(Collectors.toList());
    }
}