package com._1000meal.global.error.exception;



// 모든 ErrorCode(UserErrorCode, MenuErrorCode 등)를 핸들러에서 호환시키기 위해 Interface 형태로 작성
public interface ErrorCodeIfs {

    Integer getHttpStatusCode();
    String getMessage();
}