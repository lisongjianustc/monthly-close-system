package com.mc.common.exception;

import com.mc.common.result.Result;
import com.mc.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: path={}, code={}, message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常: path={}, message={}", request.getRequestURI(), message);
        return Result.fail(ResultCode.VALIDATE_FAIL.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("绑定异常: path={}, message={}", request.getRequestURI(), message);
        return Result.fail(ResultCode.VALIDATE_FAIL.getCode(), message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = "缺少参数: " + e.getParameterName();
        log.warn("请求参数缺失: path={}, message={}", request.getRequestURI(), message);
        return Result.fail(ResultCode.VALIDATE_FAIL.getCode(), message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数 %s 类型错误", e.getName());
        log.warn("参数类型不匹配: path={}, message={}", request.getRequestURI(), message);
        return Result.fail(ResultCode.VALIDATE_FAIL.getCode(), message);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = String.format("不支持 %s 方法", e.getMethod());
        log.warn("请求方法不支持: path={}, message={}", request.getRequestURI(), message);
        return Result.fail(ResultCode.VALIDATE_FAIL.getCode(), message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传大小超限: path={}", request.getRequestURI());
        return Result.fail(ResultCode.IMPORT_FILE_TOO_LARGE.getCode(), "文件大小超过限制");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("404: path={}", request.getRequestURI());
        return Result.fail(ResultCode.NOT_FOUND.getCode(), "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: path={}", request.getRequestURI(), e);
        return Result.fail(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误，请稍后重试");
    }
}
