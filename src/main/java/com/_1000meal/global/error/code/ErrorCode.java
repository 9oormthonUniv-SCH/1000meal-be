package com._1000meal.global.error.code;

import com._1000meal.global.error.exception.ErrorCodeIfs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// 서비스 내에서 사용될 공통 에러(Internal Server Error 등)
@Getter
@AllArgsConstructor
public enum ErrorCode implements ErrorCodeIfs {

    SAMPLE_ERROR(500, "Sample Error"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내에서 예기치 못한 에러가 발생했습니다.")
    ;

    private final Integer httpStatusCode;
    private final String message;
}