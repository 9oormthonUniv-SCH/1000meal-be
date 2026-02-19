package com._1000meal.qr.api.dto;

import jakarta.validation.constraints.NotBlank;

public record QrUsageRequest(
        @NotBlank String qrToken
) {}
