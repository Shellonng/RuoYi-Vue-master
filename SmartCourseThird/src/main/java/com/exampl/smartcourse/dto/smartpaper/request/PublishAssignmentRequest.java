package com.exampl.smartcourse.dto.smartpaper.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 发布作业请求DTO
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@Schema(description = "发布作业请求")
public class PublishAssignmentRequest {

    @NotNull(message = "作业ID不能为空")
    @Schema(description = "作业ID", example = "1")
    private Long assignmentId;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", example = "2025-11-17T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", example = "2025-11-20T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "作业模式：question-题目模式，file-文件提交模式", example = "question")
    private String mode = "question";

    @Schema(description = "任务时长（分钟，用于估算完成时间）", example = "60")
    private Integer duration;

    @Schema(description = "允许的文件类型（JSON数组字符串，仅file模式）",
            example = "[\"pdf\", \"doc\", \"docx\"]")
    private String allowedFileTypes;

    @Schema(description = "附件列表（JSON数组字符串），格式示例：[{\"name\":\"文件名.pdf\",\"url\":\"https://xx.com/file.pdf\"}]",
            example = "[{\"name\":\"补充材料.pdf\",\"url\":\"/files/supplement.pdf\"}]")
    private String attachments;
}
