package com._1000meal.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 아이디(관리자ID) 또는 이메일로 요청 */
public record PasswordResetRequest(
        @NotBlank String userIdOrEmail
) {}