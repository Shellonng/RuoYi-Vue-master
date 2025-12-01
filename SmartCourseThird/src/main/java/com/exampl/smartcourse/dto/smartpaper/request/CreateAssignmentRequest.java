package com.exampl.smartcourse.dto.smartpaper.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 手动创建作业请求
 */
@Data
@Schema(description = "手动创建作业请求")
public class CreateAssignmentRequest {

    @NotBlank(message = "作业标题不能为空")
    @Schema(description = "作业标题", example = "第一章作业")
    private String title;

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", example = "40001")
    private Long courseId;

    @NotBlank(message = "作业类型不能为空")
    @Schema(description = "作业类型：homework/exam", example = "homework")
    private String type;

    @Schema(description = "作业描述", example = "覆盖第一章全部知识点")
    private String description;

    @NotEmpty(message = "题目列表不能为空")
    @Schema(description = "题目ID列表", example = "[80001,80002]")
    private List<Long> questionIds;

    @Schema(description = "总分，不填默认100分", example = "100")
    private Integer totalScore = 100;

    @Schema(description = "答题时限（分钟），仅考试类型有效", example = "60")
    private Integer timeLimit;

    @Schema(description = "开始时间", example = "2025-11-17T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-11-20T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "允许的文件类型（JSON数组字符串，仅file模式）", example = "[\"pdf\", \"doc\", \"docx\"]")
    private String allowedFileTypes;

    @Schema(description = "附件列表（JSON数组字符串），格式示例：[{\"name\":\"文件名.pdf\",\"url\":\"https://xx.com/file.pdf\"}]", example = "[{\"name\":\"大纲.pdf\",\"url\":\"/files/syllabus.pdf\"}]")
    private String attachments;
}
