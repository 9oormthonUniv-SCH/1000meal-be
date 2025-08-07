package com._1000meal.global.error.code;

import com._1000meal.global.error.exception.ErrorCodeIfs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements ErrorCodeIfs {

    SAMPLE_ERROR("COMMON_500", 500, "Sample Error"),
    INTERNAL_SERVER_ERROR("COMMON_500", 500, "서버 내에서 예기치 못한 에러가 발생했습니다.");

    private final String code;       // ← 추가
    private final Integer httpStatusCode;
    private final String message;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}