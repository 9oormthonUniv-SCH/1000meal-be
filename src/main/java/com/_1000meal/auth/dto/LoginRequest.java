package com._1000meal.auth.dto;


import com._1000meal.global.constant.Role;

public record LoginRequest(
        Role role,              // STUDENT / ADMIN
        String usernameOrEmail, // 로그인 식별자
        String password
) {}