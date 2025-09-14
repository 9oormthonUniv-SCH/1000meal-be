package com._1000meal.auth.dto.EmailChange;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record EmailChangeStartRequest(
        @NotBlank @Email String currentEmail,
        @NotBlank String password
) {}