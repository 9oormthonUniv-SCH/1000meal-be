package com._1000meal.global.error;


import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.error.exception.ErrorCodeIfs;
import com._1000meal.global.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
@Order(Integer.MIN_VALUE)
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustom(CustomException ex) {
        ErrorCodeIfs code = ex.getErrorCodeIfs();
        String message = ex.getDisplayMessage(); // ← 커스텀 메시지 우선

        // 로그에 기본/커스텀 모두 남겨 디버깅 용이
        log.error("[CustomException] code={}, http={}, defaultMsg='{}', customMsg='{}'",
                code.getCode(), code.getHttpStatusCode(), code.getMessage(), ex.getCustomMessage());

        // Result 에 커스텀 메시지를 실어 내려보내기
        Result result = Result.error(code, message); // ← 아래 2) 오버로드 추가 필요

        return ResponseEntity.status(code.getHttpStatusCode()).body(result);
    }

    // (선택) 그 밖의 예외 공통 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIAE(IllegalArgumentException ex) {
        var code = com._1000meal.global.error.code.ErrorCode.VALIDATION_ERROR;
        log.warn("[IllegalArgumentException] {}", ex.getMessage());
        return ResponseEntity.status(code.getHttpStatusCode())
                .body(Result.error(code, ex.getMessage()));
    }

    // (선택) 마지막 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOthers(Exception ex) {
        var code = com._1000meal.global.error.code.ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("[Unhandled] ", ex);
        return ResponseEntity.status(code.getHttpStatusCode())
                .body(Result.error(code, code.getMessage()));
    }
}