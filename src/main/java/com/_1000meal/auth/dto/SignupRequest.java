package com._1000meal.auth.dto;

import com._1000meal.global.constant.Role;

import com._1000meal.global.constant.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        Role role,                       // ADMIN | STUDENT  (필수)
        @NotBlank String userId,         // 관리자 ID 또는 학번(학생 8자리)
        @NotBlank String name,           // 표시 이름
        @NotBlank @Email String email,   // 학생: sch.ac.kr 필수, 관리자: 일반 이메일 허용
        @NotBlank
        @Pattern(
                regexp = "^(?=\\S+$).{8,64}$",
                message = "비밀번호 형식이 올바르지 않습니다."
        )
        String password
) {}