package com.exampl.smartcourse.entity.aiGrading;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseResource {
    private Long id;
    private Long courseId;
    private String name;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String description;
    private Integer downloadCount;
    private Long uploadUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}