package com._1000meal.global.error.code;

import com._1000meal.global.error.exception.ErrorCodeIfs;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements ErrorCodeIfs {

    // ===== 공용(HTTP 표준 맵핑) =====
    BAD_REQUEST("COMMON_400", 400, "잘못된 요청입니다."),
    VALIDATION_ERROR("COMMON_400_VALIDATION", 400, "요청 값 검증에 실패했습니다."),
    UNAUTHORIZED("AUTH_401", 401, "인증이 필요합니다."),
    FORBIDDEN("AUTH_403", 403, "권한이 없습니다."),
    NOT_FOUND("COMMON_404", 404, "리소스를 찾을 수 없습니다."),
    CONFLICT("COMMON_409", 409, "요청이 현재 리소스 상태와 충돌합니다."),
    PRECONDITION_REQUIRED("COMMON_428", 428, "요청 선행 조건이 충족되어야 합니다."),
    TOO_MANY_REQUESTS("COMMON_429", 429, "너무 많은 요청이 발생했습니다."), // ★ 추가

    // ===== 계정/인증 도메인 =====
    ACCOUNT_INACTIVE("ACCOUNT_423", 423, "계정이 활성화되지 않았습니다."), // 423 Locked
    DUPLICATE_USER_ID("ACCOUNT_409_USERID", 409, "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL("ACCOUNT_409_EMAIL", 409, "이미 사용 중인 이메일입니다."),
    ROLE_MISMATCH("AUTH_409_ROLE", 409, "요청한 역할과 계정 역할이 일치하지 않습니다."),
    USER_NOT_FOUND("ACCOUNT_404_USER", 404, "사용자를 찾을 수 없습니다."), // ★ 추가

    // ===== 이메일 인증/비밀번호 재설정 도메인 =====
    EMAIL_NOT_VERIFIED("EMAIL_428", 428, "이메일 인증이 필요합니다."),
    EMAIL_CODE_MISMATCH("EMAIL_400", 400, "인증 코드가 일치하지 않습니다."),
    EMAIL_CODE_EXPIRED("EMAIL_410", 410, "인증 코드가 만료되었습니다."),
    INVALID_TOKEN("AUTH_400_TOKEN", 400, "유효하지 않거나 만료된 토큰입니다."), // ★ 추가

    // ===== 샘플 & 서버 내부 =====
    SAMPLE_ERROR("COMMON_500_SAMPLE", 500, "Sample Error"),
    INTERNAL_SERVER_ERROR("COMMON_500", 500, "서버 내에서 예기치 못한 에러가 발생했습니다.");

    private final String code;          // 비즈니스 코드
    private final Integer httpStatusCode;
    private final String message;

    @Override public String getCode() { return code; }
    @Override public Integer getHttpStatusCode() { return httpStatusCode; }
    @Override public String getMessage() { return message; }
}