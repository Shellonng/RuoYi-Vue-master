package com.exampl.smartcourse.dto.smartpaper.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目详情响应DTO
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@Schema(description = "题目详情响应")
public class QuestionDetailResponse {

    @Schema(description = "题目ID", example = "1")
    private Long id;

    @Schema(description = "题干内容", example = "以下哪个是Java的关键字？")
    private String title;

    @Schema(description = "题型", example = "single_choice")
    private String questionType;

    @Schema(description = "题型名称", example = "单选题")
    private String questionTypeName;

    @Schema(description = "难度等级(1-5)", example = "3")
    private Integer difficulty;

    @Schema(description = "难度名称", example = "中等")
    private String difficultyName;

    @Schema(description = "标准答案", example = "A")
    private String correctAnswer;

    @Schema(description = "答案解析", example = "abstract是Java的关键字，用于定义抽象类和抽象方法")
    private String explanation;

    @Schema(description = "知识点", example = "Java基础语法")
    private String knowledgePoint;

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "章节ID", example = "1")
    private Long chapterId;

    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建人姓名", example = "张老师")
    private String createdByName;

    @Schema(description = "题目选项列表")
    private List<QuestionOptionResponse> options;

    @Schema(description = "创建时间", example = "2025-11-17T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-17T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 获取题型名称
     */
    public String getQuestionTypeName() {
        if (questionType == null) return "";
        switch (questionType) {
            case "single":
                return "单选题";
            case "multiple":
                return "多选题";
            case "true_false":
                return "判断题";
            case "blank":
                return "填空题";
            case "short":
                return "简答题";
            case "code":
                return "编程题";
            default:
                return questionType;
        }
    }

    /**
     * 获取难度名称
     */
    public String getDifficultyName() {
        if (difficulty == null) return "";
        switch (difficulty) {
            case 1:
                return "非常简单";
            case 2:
                return "简单";
            case 3:
                return "中等";
            case 4:
                return "困难";
            case 5:
                return "非常困难";
            default:
                return String.valueOf(difficulty);
        }
    }
}
