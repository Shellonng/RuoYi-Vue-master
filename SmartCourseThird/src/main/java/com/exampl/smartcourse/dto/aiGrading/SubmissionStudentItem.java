package com.exampl.smartcourse.dto.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.AssignmentSubmission;
import com.exampl.smartcourse.entity.aiGrading.Student;
import lombok.Data;

@Data
public class SubmissionStudentItem {
    private AssignmentSubmission submission;
    private Student student;
    private Long userId;
    private String realName;
}