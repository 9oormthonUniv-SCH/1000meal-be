package com._1000meal.global.error.exception;

import lombok.Getter;

// 서비스 내 모든 커스텀 예외는 이 클래스를 상속받아 사용
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCodeIfs errorCodeIfs;

    public CustomException(ErrorCodeIfs errorCodeIfs) {
        super(errorCodeIfs.getMessage());
        this.errorCodeIfs = errorCodeIfs;
    }

    // (선택) 필요하면 cause(원인)도 받는 생성자 추가
    public CustomException(ErrorCodeIfs errorCodeIfs, Throwable cause) {
        super(errorCodeIfs.getMessage(), cause);
        this.errorCodeIfs = errorCodeIfs;
    }
}