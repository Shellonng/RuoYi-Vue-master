package com.exampl.smartcourse.entity.aiGrading;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Student {
    private Long id;
    private Long userId;
    private String enrollmentStatus;
    private BigDecimal gpa;
    private String gpaLevel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isDeleted;
    private LocalDateTime deleteTime;

    
}
