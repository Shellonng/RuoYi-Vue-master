package com.exampl.smartcourse.exception.smartpaper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.exampl.smartcourse.common.smartpaper.Result;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: {}, URI: {}", e.getMessage(), request.getRequestURI());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("资源不存在: {}", e.getMessage());
        return Result.error(404, e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            if (ex.getBindingResult().hasErrors()) {
                message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
            }
        }
        log.error("参数校验异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 静态资源未找到异常 - 对常见的浏览器请求降级处理
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 对于 favicon.ico、doc.html 等浏览器自动请求的资源，只记录 debug 级别
        if (uri.equals("/favicon.ico") || uri.equals("/doc.html")) {
            log.debug("静态资源未找到(可忽略): {}", uri);
        } else {
            log.warn("静态资源未找到: {}", uri);
        }
        return Result.error(404, "资源未找到");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}, URI: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.error(500, "系统异常，请联系管理员");
    }
}
