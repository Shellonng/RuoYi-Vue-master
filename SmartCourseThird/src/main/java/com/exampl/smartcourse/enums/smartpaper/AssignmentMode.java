package com.exampl.smartcourse.enums.smartpaper;

import lombok.Getter;

/**
 * 作业模式枚举
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Getter
public enum AssignmentMode {
    /**
     * 答题型
     */
    QUESTION("question", "答题型"),

    /**
     * 文件上传型
     */
    FILE("file", "文件上传型");

    private final String code;
    private final String desc;

    AssignmentMode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
