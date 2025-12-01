package com.exampl.smartcourse.dto.smartpaper.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * 查询题目请求DTO
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@Schema(description = "查询题目请求")
public class QueryQuestionRequest {

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "章节ID", example = "1")
    private Long chapterId;

    @Schema(description = "题型：single-单选题，multiple-多选题，true_false-判断题，blank-填空题，short-简答题，code-编程题",
            example = "single")
    private String questionType;

    @Schema(description = "难度等级(1-5)", example = "3")
    private Integer difficulty;

    @Schema(description = "知识点（模糊查询）", example = "Java")
    private String knowledgePoint;

    @Schema(description = "题干内容（模糊查询）", example = "关键字")
    private String title;

    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "排序字段：create_time-创建时间，difficulty-难度", example = "create_time")
    private String sortField = "create_time";

    @Schema(description = "排序方式：asc-升序，desc-降序", example = "desc")
    private String sortOrder = "desc";
}
