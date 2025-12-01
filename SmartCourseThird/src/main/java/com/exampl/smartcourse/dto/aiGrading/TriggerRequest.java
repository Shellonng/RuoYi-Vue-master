package com.exampl.smartcourse.dto.aiGrading;

import lombok.Data;

import java.util.List;

@Data
public class TriggerRequest {
    private Long submissionId;
    private List<Long> submissionIds;
    private String llmModel;
}