package com._1000meal.global.response;

import com._1000meal.global.error.exception.ErrorCodeIfs;
import com._1000meal.global.error.code.SuccessCode;

import java.time.LocalDateTime;

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
                errorCodeIfs.getMessage(),  // 기본 메시지 사용
                LocalDateTime.now()
        );
    }

    /** ✅ 커스텀 메시지를 반영하는 오버로드 추가 */
    public static Result error(ErrorCodeIfs errorCodeIfs, String customMessage) {
        return new Result(
                errorCodeIfs.getCode(),
                (customMessage != null && !customMessage.isBlank())
                        ? customMessage
                        : errorCodeIfs.getMessage(), // customMessage가 있으면 우선
                LocalDateTime.now()
        );
    }
}