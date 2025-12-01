package com.exampl.smartcourse.dto.smartpaper.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新题目请求DTO
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@Schema(description = "更新题目请求")
public class UpdateQuestionRequest {

    @NotNull(message = "题目ID不能为空")
    @Schema(description = "题目ID", example = "1")
    private Long id;

    @Schema(description = "题干内容", example = "以下哪个是Java的关键字？")
    private String title;

    @Schema(description = "题型：single-单选题，multiple多选题，true_false-判断题，blank-填空题，short-简答题, code-编程题",
            example = "single")
    private String questionType;

    @Min(value = 1, message = "难度等级最小为1")
    @Max(value = 5, message = "难度等级最大为5")
    @Schema(description = "难度等级(1-5)，1最简单，5最难", example = "3")
    private Integer difficulty;

    @Schema(description = "标准答案", example = "A")
    private String correctAnswer;

    @Schema(description = "答案解析", example = "abstract是Java的关键字")
    private String explanation;

    @Schema(description = "知识点", example = "Java基础语法")
    private String knowledgePoint;

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "章节ID", example = "1")
    private Long chapterId;

    @Schema(description = "题目选项列表（仅选择题和判断题需要）")
    private List<CreateQuestionRequest.QuestionOptionDTO> options;
}
