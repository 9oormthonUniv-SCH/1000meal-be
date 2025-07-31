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
@Order(value = Integer.MIN_VALUE)
public class CustomExceptionHandler {

    // Custom 예외를 잡는 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> customExceptionHandler(CustomException customException) {
        log.error("custom exception handler : {}", customException.getErrorCodeIfs().getMessage());

        ErrorCodeIfs errorCodeIfs = customException.getErrorCodeIfs();

        Result result = Result.error(errorCodeIfs);

        return ResponseEntity
                .status(errorCodeIfs.getHttpStatusCode())
                .body(result);
    }
}