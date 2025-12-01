package com.exampl.smartcourse.entity.aiGrading;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgePoint {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String level;
    private Long creatorUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isDeleted;
    private LocalDateTime deleteTime;
}