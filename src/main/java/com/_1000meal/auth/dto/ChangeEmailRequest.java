package com._1000meal.auth.dto;

import lombok.Builder;

@Builder
public record ChangeEmailRequest(
        String currentPassword,
        String newEmail
) {}