package com._1000meal.global.error;

import com._1000meal.global.error.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com._1000meal.global.response.Result;


@RestControllerAdvice
@Slf4j
@Order(value = Integer.MAX_VALUE)
public class GlobalExceptionHandler {

    // 글로벌 예외를 잡는 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> GlobalExceptionHandler(Exception e) {
        log.error("Unexpected Server Error : {}", e.getMessage());

        Result result = Result.error(ErrorCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(result.statusCode())
                .body(result);
    }
}