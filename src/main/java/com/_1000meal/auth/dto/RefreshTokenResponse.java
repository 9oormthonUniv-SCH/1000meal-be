package com._1000meal.auth.dto;

public record RefreshTokenResponse(
        String accessToken,
        long expiresInSeconds
) {
}
