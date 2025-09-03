package com._1000meal.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

//public record PasswordResetConfirmRequest(
//        @NotBlank String token,
//        @NotBlank String newPassword,
//        @NotBlank String confirmPassword
//) {}


public record PasswordResetConfirmRequest(
        @NotBlank @Email String email,

        // 6자리 숫자 토큰 (선행 0 허용 → String)
        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
        String token,

        @NotBlank String newPassword,
        @NotBlank String confirmPassword
) {}