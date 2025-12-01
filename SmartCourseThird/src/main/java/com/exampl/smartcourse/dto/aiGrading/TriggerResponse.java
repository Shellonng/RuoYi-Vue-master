package com.exampl.smartcourse.dto.aiGrading;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TriggerResponse {
    private List<Long> accepted;
    private List<Long> failed;
    private String message;
}