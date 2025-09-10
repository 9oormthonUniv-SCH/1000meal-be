package com._1000meal.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeleteAccountRequest(
        @NotBlank String currentPassword,  // 현재 비밀번호 확인용
        @NotNull Boolean agree             // 탈퇴 동의 여부
) {}