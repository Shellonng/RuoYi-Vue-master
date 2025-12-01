package com.exampl.smartcourse.dto.aiGrading;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradingModificationRequest {
    private BigDecimal contentScore;
    private BigDecimal logicScore;
    private BigDecimal knowledgeScore;
    private BigDecimal innovationScore;
    private BigDecimal totalScore;
    private String aiComment;
    private String improvementSuggestions;
    private String coveredKnowledgePoints;
    private String missingKnowledgePoints;
    private String llmModel;
    private String teacherComment;
}
