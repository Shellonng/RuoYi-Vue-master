package com.exampl.smartcourse.dto.smartpaper.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新作业请求
 */
@Data
@Schema(description = "更新作业请求")
public class UpdateAssignmentRequest {

    @Schema(description = "作业标题", example = "第一章作业（更新版）")
    private String title;

    @Schema(description = "作业描述", example = "更新了题目与分值")
    private String description;

    @Schema(description = "总分", example = "120")
    private Integer totalScore;

    @Schema(description = "时间限制（分钟）", example = "90")
    private Integer timeLimit;

    @Schema(description = "预估任务时长（分钟）", example = "60")
    private Integer duration;

    @Schema(description = "开始时间", example = "2025-11-17T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-11-20T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "允许的文件类型（JSON数组字符串，仅file模式）", example = "[\"pdf\", \"doc\", \"docx\"]")
    private String allowedFileTypes;

    @Schema(description = "附件列表（JSON数组字符串）", example = "[{\"name\":\"参考.pdf\",\"url\":\"/files/ref.pdf\"}]")
    private String attachments;

    @Schema(description = "新的题目ID列表，传空数组表示清空题目，null 表示不修改", example = "[80001,80003]")
    private List<Long> questionIds;
}
