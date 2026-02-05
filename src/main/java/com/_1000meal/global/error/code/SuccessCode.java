package com._1000meal.global.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SuccessCode {

    // 2xx 번대 응답 코드
    OK("SUCCESS_200", "성공"),
    UPDATED("SUCCESS_200", "성공적으로 업데이트 되었습니다."),
    CREATED("SUCCESS_201", "성공적으로 생성되었습니다."), // ← 이 부분 추가!
    ;

    private final String code;
    private final String message;
}