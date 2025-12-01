package com.exampl.smartcourse.entity.smartpaper;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * 题目选项实体类
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Data
@TableName("question_option")
public class QuestionOption implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 选项ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 选项标识(A/B/C/D/T/F)
     */
    private String optionLabel;

    /**
     * 选项内容
     */
    private String optionText;
}
