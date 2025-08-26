package com._1000meal.auth.dto;

import com._1000meal.global.constant.Role;

public record SignupResponse(
        Long accountId,
        Role role,              // STUDENT / ADMIN
        String userId,          // 학번 또는 관리자 ID
        String email,
        String status,          // PENDING / ACTIVE / SUSPENDED
        Long storeId,            // 관리자 전용, 학생은 null
        String storeName  // ADMIN일 때만 값, STUDENT는 null
) {}