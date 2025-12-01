package com.exampl.smartcourse.dto.auth;

import lombok.Data;

@Data
public class CourseBrief {
    private Long id;
    private String title;
    private String description;
    private String term;
    private String status;
    private Long teacherUserId;
    private Integer studentCount;
}
