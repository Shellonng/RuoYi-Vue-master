package com.exampl.smartcourse.exception.smartpaper;

/**
 * 资源不存在异常
 *
 * @author 开发团队
 * @since 2025-11-17
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
