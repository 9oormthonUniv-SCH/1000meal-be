package com._1000meal.auth.dto;

public record ChangeEmailRequest(
        String currentPassword,
        String newEmail
) {}