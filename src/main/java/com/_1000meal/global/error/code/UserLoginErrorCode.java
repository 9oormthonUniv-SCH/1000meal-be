package com._1000meal.global.error.code;

import com._1000meal.global.error.exception.ErrorCodeIfs;


public enum UserLoginErrorCode implements ErrorCodeIfs {
    USER_NOT_AUTHENTICATED("USER_401", 401, "로그인이 필요합니다."),
    USER_NOT_FOUND("USER_404", 404, "존재하지 않는 사용자입니다."),
    INTERNAL_SERVER_ERROR("USER_500", 500, "서버 오류가 발생했습니다.");

    private final String code;          // ← 추가!
    private final Integer httpStatusCode;
    private final String message;

    UserLoginErrorCode(String code, Integer httpStatusCode, String message) {
        this.code = code;
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

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