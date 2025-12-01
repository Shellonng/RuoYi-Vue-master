package com.exampl.smartcourse.enums.smartpaper;

import lombok.Getter;

/**
 * 题目类型枚举
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Getter
public enum QuestionType {
    /**
     * 单选题
     */
    SINGLE("single", "单选题"),

    /**
     * 多选题
     */
    MULTIPLE("multiple", "多选题"),

    /**
     * 判断题
     */
    TRUE_FALSE("true_false", "判断题"),

    /**
     * 填空题
     */
    BLANK("blank", "填空题"),

    /**
     * 简答题
     */
    SHORT("short", "简答题"),

    /**
     * 编程题
     */
    CODE("code", "编程题");

    private final String code;
    private final String desc;

    QuestionType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static QuestionType fromCode(String code) {
        for (QuestionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的题型: " + code);
    }
}
