package com.exampl.smartcourse.entity.smartpaper;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * 作业题目关联实体类
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@TableName("assignment_question")
public class AssignmentQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业ID
     */
    private Long assignmentId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 题目分值
     */
    private Integer score;

    /**
     * 题目顺序
     */
    private Integer sequence;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer isDeleted;
}
