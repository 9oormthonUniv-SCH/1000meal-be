package com._1000meal.auth.dto;

import com._1000meal.global.constant.Role;

public record SignupResponse(
        Long accountId,
        Role role,              // STUDENT / ADMIN
        String username,
        String email,
        String status           // PENDING / ACTIVE / SUSPENDED
) {}