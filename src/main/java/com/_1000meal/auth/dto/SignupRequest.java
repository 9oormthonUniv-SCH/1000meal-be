package com._1000meal.auth.dto;

import com._1000meal.global.constant.Role;

public record SignupRequest(
        Role role,              // STUDENT / ADMIN
        String username,        // 학생=학번, 관리자=아이디
        String email,           // 학생=학교 이메일, 관리자=선택 이메일
        String password,

        // STUDENT 전용
        String name,
        String department,
        String phone,

        // ADMIN 전용
        String displayName,
        Integer adminLevel
) {}