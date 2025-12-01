package com.exampl.smartcourse.dto.smartpaper.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建题目请求DTO
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@Schema(description = "创建题目请求")
public class CreateQuestionRequest {

    @NotBlank(message = "题干内容不能为空")
    @Schema(description = "题干内容", example = "解释一下transformer的Q、K、V矩阵是如何得到的")
    private String title;

    @NotBlank(message = "题型不能为空")
    @Schema(description = "题型：single-单选题，multiple-多选题，true_false-判断题，blank-填空题，short-简答题",
            example = "blank")
    private String questionType;

    @NotNull(message = "难度等级不能为空")
    @Min(value = 1, message = "难度等级最小为1")
    @Max(value = 5, message = "难度等级最大为5")
    @Schema(description = "难度等级(1-5)，1最简单，5最难", example = "4")
    private Integer difficulty;

    @NotBlank(message = "标准答案不能为空")
    @Schema(description = "标准答案，单选题为选项标识(如A)，多选题为逗号分隔(如A,C)，判断题为T/F", example = "")
    private String correctAnswer;

    @Schema(description = "答案解析", example = "假设我们有一个输入序列，比如“I love cats”，每个单词都已经转换成了一个向量（称为词嵌入）。\r\n" + //
                "\r\n" + //
                "第1步：从输入向量开始\r\n" + //
                "\r\n" + //
                "我们有一个输入矩阵 X，其每一行代表一个单词的向量（例如，x1 对应 “I”，x2 对应 “love”，x3 对应 “cats”）。\r\n" + //
                "\r\n" + //
                "第2步：使用可学习的权重矩阵进行线性变换\r\n" + //
                "\r\n" + //
                "Transformer 初始化了三个独立的、可训练的权重矩阵：W_Q, W_K, W_V。\r\n" + //
                "\r\n" + //
                "W_Q（Query权重矩阵）：负责将输入向量投影到“查询”空间。\r\n" + //
                "\r\n" + //
                "W_K（Key权重矩阵）：负责将输入向量投影到“键”空间。\r\n" + //
                "\r\n" + //
                "W_V（Value权重矩阵）：负责将输入向量投影到“值”空间。\r\n" + //
                "\r\n" + //
                "第3步：计算 Q, K, V 矩阵\r\n" + //
                "\r\n" + //
                "计算非常简单，就是输入矩阵 X 分别与这三个权重矩阵相乘：\r\n" + //
                "\r\n" + //
                "Q = X * W_Q （Query 矩阵）\r\n" + //
                "\r\n" + //
                "K = X * W_K （Key 矩阵）\r\n" + //
                "\r\n" + //
                "V = X * W_V （Value 矩阵）")
    private String explanation;

    @NotBlank(message = "知识点不能为空")
    @Schema(description = "知识点", example = "60001")
    private String knowledgePoint;

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", example = "40001")
    private Long courseId;

    @NotNull(message = "章节ID不能为空")
    @Schema(description = "章节ID", example = "72001")
    private Long chapterId;

    @Schema(description = "题目选项列表（仅选择题和判断题需要）")
    private List<QuestionOptionDTO> options;

    /**
     * 题目选项DTO
     */
    @Data
    @Schema(description = "题目选项")
    public static class QuestionOptionDTO {

        @NotBlank(message = "选项标识不能为空")
        @JsonAlias({"optionKey", "option_label"})
        @Schema(description = "选项标识(A/B/C/D/T/F)", example = " ")
        private String optionLabel;

        @NotBlank(message = "选项内容不能为空")
        @JsonAlias({"content", "option_text"})
        @Schema(description = "选项内容", example = " ")
        private String optionText;
    }
}
