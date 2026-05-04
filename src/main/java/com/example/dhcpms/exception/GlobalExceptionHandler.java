package com.example.dhcpms.exception;

import com.example.dhcpms.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

/**
 * 全局异常处理器（整合原有逻辑 + 新增业务异常处理）
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. 处理权限不足异常（保留原有逻辑，优化返回格式）
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<?>> handleAccessDeniedException(AccessDeniedException e) {
        Result<?> result = Result.error("权限不足，无法访问");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    // 2. 处理自定义业务异常（新增核心逻辑）
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        Result<?> result = Result.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    // 3. 处理通用异常（保留原有逻辑，优化返回格式 + 打印异常栈）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleGeneralException(Exception e) {
        // 打印异常栈，方便后端排查问题
        e.printStackTrace();
        Result<?> result = Result.error("服务器内部错误，请联系管理员");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}