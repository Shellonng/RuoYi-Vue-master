package com.exampl.smartcourse.dto.smartpaper.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业响应DTO（列表展示）
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@Schema(description = "作业响应")
public class AssignmentResponse {

    @Schema(description = "作业ID", example = "1")
    private Long id;

    @Schema(description = "作业标题", example = "第一章测试")
    private String title;

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "课程名称", example = "Java程序设计")
    private String courseName;

    @Schema(description = "发布者ID", example = "1")
    private Long publisherUserId;

    @Schema(description = "发布者姓名", example = "张老师")
    private String publisherName;

    @Schema(description = "类型：homework-作业，exam-考试", example = "homework")
    private String type;

    @Schema(description = "类型名称", example = "作业")
    private String typeName;

    @Schema(description = "描述", example = "第一章综合测试")
    private String description;

    @Schema(description = "开始时间", example = "2025-11-17T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-11-20T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "发布状态：0-未发布，1-已发布", example = "1")
    private Integer status;

    @Schema(description = "状态名称", example = "已发布")
    private String statusName;

    @Schema(description = "作业模式：question-题目模式，file-文件提交模式", example = "question")
    private String mode;

    @Schema(description = "时间限制（分钟）", example = "60")
    private Integer timeLimit;

    @Schema(description = "总分", example = "100")
    private Integer totalScore;

    @Schema(description = "任务时长（分钟）", example = "60")
    private Integer duration;

    @Schema(description = "允许的文件类型（JSON数组字符串，仅file模式）", example = "[\"pdf\",\"doc\",\"docx\"]")
    private String allowedFileTypes;

    @Schema(description = "附件列表（JSON数组字符串）", example = "[{\"name\":\"大纲.pdf\",\"url\":\"/files/syllabus.pdf\"}]")
    private String attachments;

    @Schema(description = "题目数量", example = "20")
    private Integer questionCount;

    @Schema(description = "提交人数", example = "30")
    private Integer submissionCount;

    @Schema(description = "创建时间", example = "2025-11-17T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-11-17T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 获取类型名称
     */
    public String getTypeName() {
        if (type == null) return "";
        return "homework".equals(type) ? "作业" : "exam".equals(type) ? "考试" : type;
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (status == null) return "";
        return status == 1 ? "已发布" : "未发布";
    }
}
