package com.exampl.smartcourse.dto.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.entity.aiGrading.AssignmentSubmission;
import com.exampl.smartcourse.entity.aiGrading.KnowledgePoint;
import com.exampl.smartcourse.entity.aiGrading.Student;
import com.exampl.smartcourse.entity.aiGrading.User;
import lombok.Data;
import java.util.List;

@Data
public class SubmissionDetailResponse {
    private AssignmentSubmission submission;
    private Assignment assignment;
    private Student student;
    private User user;
    private Long userId;
    private String realName;
    private AiGrading grading;
    private List<KnowledgePoint> knowledgePoints;
}
