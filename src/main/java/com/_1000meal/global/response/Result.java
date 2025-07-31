package com._1000meal.global.response;

import com._1000meal.global.error.exception.ErrorCodeIfs;

import java.time.LocalDateTime;

public record Result(
        Integer statusCode,
        String message,
        LocalDateTime timestamp
) {
    public static Result ok() {
        return new Result(
                200,
                "OK",
                LocalDateTime.now()
        );
    }

    public static Result error(ErrorCodeIfs errorCodeIfs) {
        return new Result(
                errorCodeIfs.getHttpStatusCode(),
                errorCodeIfs.getMessage(),
                LocalDateTime.now()
        );
    }
}