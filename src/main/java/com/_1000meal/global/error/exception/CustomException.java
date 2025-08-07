package com._1000meal.global.error.exception;

import lombok.Getter;


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