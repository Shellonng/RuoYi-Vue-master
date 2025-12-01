package com.exampl.smartcourse.entity;

import lombok.Data;

@Data
public class Course {
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private String term;
    private String status;
    private Long teacherUserId;
    private Integer studentCount;
}
