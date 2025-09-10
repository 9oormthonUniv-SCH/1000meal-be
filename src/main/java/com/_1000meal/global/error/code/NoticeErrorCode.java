package com._1000meal.global.error.code;

import com._1000meal.global.error.exception.ErrorCodeIfs;

public enum NoticeErrorCode implements ErrorCodeIfs {


    NOTICE_NOT_FOUND("NOTICE_404", 404, "공지가 없습니다.");

    private final String code;
    private final Integer httpStatusCode;
    private final String message;

    NoticeErrorCode(String code, Integer httpStatusCode, String message) {
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
