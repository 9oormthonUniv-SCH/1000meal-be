package com._1000meal.global.response;

import com._1000meal.global.error.exception.ErrorCodeIfs;

import java.time.LocalDateTime;



public record Result<T>(
        Integer statusCode,
        String message,
        LocalDateTime timestamp,
        T data
) {
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "OK", LocalDateTime.now(), data);
    }

    public static Result<?> error(ErrorCodeIfs code) {
        return new Result<>(code.getHttpStatusCode(), code.getMessage(), LocalDateTime.now(), null);
    }
}