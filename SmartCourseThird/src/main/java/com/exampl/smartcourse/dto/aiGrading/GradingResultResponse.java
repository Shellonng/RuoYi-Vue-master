package com.exampl.smartcourse.dto.aiGrading;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradingResultResponse {
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
    private Integer llmTokens;
    private Integer processingTime;
    private Boolean teacherConfirmed;
    private BigDecimal teacherModifiedScore;
    private String teacherComment;
    private Long confirmedBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
