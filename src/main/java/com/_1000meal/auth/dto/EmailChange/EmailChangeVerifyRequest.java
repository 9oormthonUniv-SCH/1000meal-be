package com._1000meal.auth.dto.EmailChange;

// VERIFY: 기존 confirm과 맞추기 위해 newEmail + code
import jakarta.validation.constraints.NotBlank;


public record EmailChangeVerifyRequest(
        @NotBlank String changeId,
        @NotBlank String code
) {}