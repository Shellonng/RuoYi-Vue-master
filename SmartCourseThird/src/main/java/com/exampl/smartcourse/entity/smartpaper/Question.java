package com.exampl.smartcourse.entity.smartpaper;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目实体类
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@TableName("question")
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 题干内容
     */
    private String title;

    /**
     * 题型
     */
    private String questionType;

    /**
     * 难度等级(1-5)
     */
    private Integer difficulty;

    /**
     * 标准答案
     */
    private String correctAnswer;

    /**
     * 答案解析
     */
    private String explanation;

    /**
     * 知识点
     */
    private String knowledgePoint;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 创建人ID
     */
    private Long createdBy;

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
