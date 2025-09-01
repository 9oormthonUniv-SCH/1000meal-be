package com._1000meal.global.error.code;

import com._1000meal.global.error.exception.ErrorCodeIfs;

public enum MenuErrorCode implements ErrorCodeIfs {

    MENU_EMPTY("MENU_404", 404, "메뉴를 입력하지 않았습니다."),
    MENU_NOT_FOUND("MENU_404", 404, "존재하지 않는 메뉴입니다."),
    DAILY_MENU_NOT_FOUND("DAILY_MENU_404", 404, "해당 날짜의 메뉴가 존재하지 않습니다."),
    WEEKLY_MENU_NOT_FOUND("WEEKLY_MENU_404", 404, "해당 주간의 메뉴가 존재하지 않습니다."),
    MENU_ALREADY_EXISTS("MENU_409", 409, "이미 존재하는 메뉴입니다."),
    INVALID_MENU_NAME("MENU_400", 400, "잘못된 메뉴 이름입니다."),
    INTERNAL_SERVER_ERROR("MENU_500", 500, "서버 오류가 발생했습니다."),
    INSUFFICIENT_STOCK("DAILY_MENU_500", 500, "해당 메뉴의 재고가 요청량보다 작습니다."),
    DATE_REQUIRED("MENU_400", 400, "날짜가 누락됐습니다."),
    ;

    private final String code;
    private final Integer httpStatusCode;
    private final String message;

    MenuErrorCode(String code, Integer httpStatusCode, String message) {
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
