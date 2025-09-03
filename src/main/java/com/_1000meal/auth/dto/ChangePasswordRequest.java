package com._1000meal.auth.dto;


import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {}