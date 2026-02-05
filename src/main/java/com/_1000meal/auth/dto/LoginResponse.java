package com._1000meal.auth.dto;

import com._1000meal.global.constant.Role;

public record LoginResponse(
        Long accountId,
        Role role,
        String userId,       // username -> userId 로 변경
        String email,
        String accessToken,
        String refreshToken,
        Long storeId,
        String storeName
) {}