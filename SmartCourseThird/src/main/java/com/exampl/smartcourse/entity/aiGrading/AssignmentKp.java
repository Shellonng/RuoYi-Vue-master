package com.exampl.smartcourse.entity.aiGrading;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentKp {
    private Long id;
    private Long assignmentId;
    private Long kpId;
    private Integer sequence;
    private Boolean isRequired;
    private LocalDateTime createTime;
}