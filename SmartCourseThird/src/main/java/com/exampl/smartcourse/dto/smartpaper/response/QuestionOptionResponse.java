package com.exampl.smartcourse.dto.smartpaper.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目选项响应DTO
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@Schema(description = "题目选项响应")
public class QuestionOptionResponse {

    @Schema(description = "选项ID", example = "1")
    private Long id;

    @Schema(description = "题目ID", example = "1")
    private Long questionId;

    @Schema(description = "选项标识(A/B/C/D/T/F)", example = "A")
    private String optionLabel;

    @Schema(description = "选项内容", example = "abstract")
    private String optionText;
}
