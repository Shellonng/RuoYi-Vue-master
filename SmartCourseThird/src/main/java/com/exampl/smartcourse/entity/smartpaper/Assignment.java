package com.exampl.smartcourse.entity.smartpaper;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作业/考试实体类
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@TableName("assignment")
@Alias("SmartPaperAssignment")
public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 作业ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业标题
     */
    private String title;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 发布者ID
     */
    private Long publisherUserId;

    /**
     * 类型(homework/exam)
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 发布状态(0-未发布，1-已发布)
     */
    private Integer status;

    /**
     * 作业模式(question/file)
     */
    private String mode;

    /**
     * 时间限制(分钟)
     */
    private Integer timeLimit;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 任务时长(分钟)
     */
    private Integer duration;

    /**
     * 允许的文件类型(JSON)
     */
    private String allowedFileTypes;

    /**
     * 任务附件列表（JSON数组字符串）
     */
    private String attachments;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;
}
