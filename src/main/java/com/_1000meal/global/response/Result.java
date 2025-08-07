package com._1000meal.global.response;

import com._1000meal.global.error.exception.ErrorCodeIfs;
import java.time.LocalDateTime;
import com._1000meal.global.error.code.SuccessCode;


public record Result(
        String code,
        String message,
        LocalDateTime timestamp
) {
    public static Result ok() {
        return new Result(
                SuccessCode.OK.getCode(),
                SuccessCode.OK.getMessage(),
                LocalDateTime.now()
        );
    }

    public static Result success(SuccessCode successCode) {
        return new Result(
                successCode.getCode(),
                successCode.getMessage(),
                LocalDateTime.now()
        );
    }

    public static Result error(ErrorCodeIfs errorCodeIfs) {
        return new Result(
                errorCodeIfs.getCode(),
                errorCodeIfs.getMessage(),
                LocalDateTime.now()
        );
    }

}