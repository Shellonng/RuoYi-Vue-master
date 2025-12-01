package com.exampl.smartcourse.enums.smartpaper;

import lombok.Getter;

/**
 * 难度等级枚举
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Getter
public enum Difficulty {
    /**
     * 简单
     */
    EASY(1, "简单"),

    /**
     * 中等
     */
    MEDIUM(3, "中等"),

    /**
     * 困难
     */
    HARD(5, "困难");

    private final Integer level;
    private final String desc;

    Difficulty(Integer level, String desc) {
        this.level = level;
        this.desc = desc;
    }

    public static Difficulty fromLevel(Integer level) {
        for (Difficulty difficulty : values()) {
            if (difficulty.getLevel().equals(level)) {
                return difficulty;
            }
        }
        return MEDIUM;
    }
}
