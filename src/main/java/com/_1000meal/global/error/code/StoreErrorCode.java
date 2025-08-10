package com._1000meal.global.error.code;

import com._1000meal.global.error.exception.ErrorCodeIfs;

public enum StoreErrorCode implements ErrorCodeIfs {
    STORE_NOT_FOUND("STORE_404", 404, "존재하지 않는 매장입니다."),
    STORE_ALREADY_EXISTS("STORE_409", 409, "이미 존재하는 매장입니다."),
    INVALID_STORE_DATA("STORE_400", 400, "잘못된 매장 데이터입니다."),
    STORE_ACCESS_DENIED("STORE_403", 403, "매장 접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("STORE_500", 500, "서버 오류가 발생했습니다.");

    private final String code;
    private final Integer httpStatusCode;
    private final String message;

    StoreErrorCode(String code, Integer httpStatusCode, String message) {
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
