package com.exampl.smartcourse.dto.smartpaper.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 智能组卷请求
 */
@Data
@Schema(description = "智能组卷请求")
public class SmartPaperRequest {

    @NotBlank(message = "作业标题不能为空")
    @Schema(description = "作业标题", example = "智能组卷测试")
    private String title;

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", example = "40001")
    private Long courseId;

    @Schema(description = "题目数量", example = "10")
    private Integer questionCount = 10;

    @Schema(description = "难度 (1-5)", example = "3")
    private Integer difficulty;

    @Schema(description = "知识点ID列表 (可选)", example = "[60001, 60002]")
    private List<Long> knowledgePointIds;

    @Schema(description = "作业类型：homework/exam", example = "homework")
    private String type = "homework";
}
