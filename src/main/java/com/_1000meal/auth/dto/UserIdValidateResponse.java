package com._1000meal.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserIdValidateResponse {
    private boolean valid;   // true면 회원가입 진행 가능
    private String status;   // AVAILABLE, TAKEN, INVALID_FORMAT, NOT_IN_ROSTER
    private String message;  // 프론트 노출용 메시지
}