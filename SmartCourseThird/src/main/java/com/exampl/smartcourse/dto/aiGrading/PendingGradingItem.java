package com.exampl.smartcourse.dto.aiGrading;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PendingGradingItem {
    private Long submissionId;
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private Long studentUserId;
    private Long studentId;
    private String enrollmentStatus;
    private Integer gradingStatus;
    private LocalDateTime submitTime;
    private String fileName;
    private String filePath;
}
