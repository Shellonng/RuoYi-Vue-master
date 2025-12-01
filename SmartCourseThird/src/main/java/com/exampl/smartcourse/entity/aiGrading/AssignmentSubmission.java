package com.exampl.smartcourse.entity.aiGrading;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentSubmission {
    private Long id;
    private Long assignmentId;
    private Long studentUserId;
    private Integer status;
    private Integer score;
    private String feedback;
    private LocalDateTime submitTime;
    private LocalDateTime gradeTime;
    private Long gradedBy;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String fileName;
    private String filePath;
    private Boolean isDeleted;
    private LocalDateTime deleteTime;

    
}
