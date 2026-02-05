package com._1000meal.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FindIdRequest(
        @NotBlank @Email String email,
        @NotBlank String name
) {}
