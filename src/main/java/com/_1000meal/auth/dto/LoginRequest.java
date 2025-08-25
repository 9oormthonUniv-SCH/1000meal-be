package com._1000meal.auth.dto;


import com._1000meal.global.constant.Role;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {}