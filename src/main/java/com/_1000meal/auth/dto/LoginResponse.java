package com._1000meal.auth.dto;

import com._1000meal.global.constant.Role;

public record LoginResponse(
        Long accountId,
        Role role,
        String username,
        String email,
        String accessToken,
        String refreshToken,
        Long storeId,      // ADMIN 인 경우만 값 존재
        String storeName   // ADMIN 인 경우만 값 존재
) {}