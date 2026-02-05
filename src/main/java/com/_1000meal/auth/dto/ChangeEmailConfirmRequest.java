package com._1000meal.auth.dto;

public record ChangeEmailConfirmRequest(
        String newEmail,
        String code
) {}
