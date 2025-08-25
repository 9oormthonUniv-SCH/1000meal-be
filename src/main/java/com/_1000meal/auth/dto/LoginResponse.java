// com._1000meal.auth.dto.LoginResponse
package com._1000meal.auth.dto;

import lombok.AllArgsConstructor; import lombok.Getter;


import com._1000meal.global.constant.Role;

public record LoginResponse(
        Long accountId,
        Role role,              // STUDENT / ADMIN
        String username,
        String email,
        String accessToken,
        String refreshToken
) {}