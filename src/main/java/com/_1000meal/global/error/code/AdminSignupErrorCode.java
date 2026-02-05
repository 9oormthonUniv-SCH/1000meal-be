package com._1000meal.global.error.code;

import com._1000meal.global.error.exception.ErrorCodeIfs;

public enum AdminSignupErrorCode implements ErrorCodeIfs {
    ADMIN_ALREADY_EXISTS("ADMIN_409", 409, "이미 등록된 관리자입니다."),
    EMAIL_ALREADY_EXISTS("ADMIN_409", 409, "이미 사용 중인 이메일입니다."),
    REQUIRED_FIELD_MISSING("ADMIN_400", 400, "필수 정보가 누락되었습니다."),
    PASSWORD_WEAK("ADMIN_400", 400,
            "비밀번호는 8~16자의 영문 대소문자, 숫자, 특수문자를 반드시 포함해야 합니다. " +
                    "아이디, 전화번호 등 개인정보를 포함하거나, 동일/연속된 문자·숫자는 사용할 수 없습니다. " +
                    "공백 없이, 세 가지 종류(영문, 숫자, 특수문자)가 모두 들어가야 안전합니다."
    ),
    PASSWORD_MISMATCH("ADMIN_400", 400, "비밀번호가 일치하지 않습니다."),
    INTERNAL_SERVER_ERROR("ADMIN_500", 500, "서버 오류가 발생했습니다.");

    private final String code;          // ← 추가!
    private final Integer httpStatusCode;
    private final String message;

    AdminSignupErrorCode(String code, Integer httpStatusCode, String message) {
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