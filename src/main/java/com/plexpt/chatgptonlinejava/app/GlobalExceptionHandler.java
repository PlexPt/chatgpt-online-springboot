package com.plexpt.chatgptonlinejava.app;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * @author plexpt
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_MSG = "服务器挤爆了，请充值或稍后尝试";

    @ExceptionHandler(RuntimeException.class)
    public Result handleBaseException(Exception ex, HttpServletRequest request) {
        log.warn("Handle exception, message={}, requestUrl={}", ex.getMessage(),
                request.getRequestURI());

        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleDefaultErrorView(Exception ex, HttpServletRequest request) {
        log.error("Handle exception, message={}, requestUrl={}", ex.getMessage(),
                request.getRequestURI(), ex);

        return Result.error(ex.getMessage());
    }

}

