package com.exampl.smartcourse.dto.smartpaper.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 生成作业/考试请求DTO（智能组卷）
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@Schema(description = "智能组卷请求")
public class GenerateAssignmentRequest {

    @NotBlank(message = "作业标题不能为空")
    @Schema(description = "作业标题", example = "第一章测试")
    private String title;

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "章节ID列表（多个章节）", example = "[1, 2, 3]")
    private List<Long> chapterIds;

    @NotBlank(message = "作业类型不能为空")
    @Schema(description = "类型：homework-作业，exam-考试", example = "homework")
    private String type;

    @Schema(description = "作业描述", example = "第一章综合测试")
    private String description;

    @NotNull(message = "题目总数不能为空")
    @Min(value = 1, message = "题目总数至少为1")
    @Schema(description = "题目总数", example = "20")
    private Integer totalQuestions;

    @Schema(description = "总分", example = "100")
    private Integer totalScore = 100;

    @Schema(description = "时间限制（分钟）", example = "60")
    private Integer timeLimit;

    @Schema(description = "组卷策略：random-随机，balanced-均衡，difficulty-按难度", example = "balanced")
    private String strategy = "balanced";

    @Schema(description = "按题型分配数量，key为题型，value为数量（single/multiple/true_false/blank/short/code）",
            example = "{\"single\": 10, \"multiple\": 5, \"true_false\": 5}")
    private Map<String, Integer> questionTypeDistribution;

    @Schema(description = "按难度分配数量，key为难度等级(1-5)，value为数量",
            example = "{\"1\": 2, \"2\": 5, \"3\": 8, \"4\": 4, \"5\": 1}")
    private Map<Integer, Integer> difficultyDistribution;

    @Schema(description = "知识点列表（指定从哪些知识点中选题）", example = "[\"Java基础语法\", \"面向对象\"]")
    private List<String> knowledgePoints;

    @Schema(description = "排除的题目ID列表", example = "[1, 2, 3]")
    private List<Long> excludeQuestionIds;
}
