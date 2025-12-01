package com.exampl.smartcourse.dto.aiGrading;

import lombok.Data;

@Data
public class SubmissionListRequest {
    private Integer page;
    private Integer pageSize;
    private String sortBy;
    private String order;
}