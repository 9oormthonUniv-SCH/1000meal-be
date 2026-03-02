package com._1000meal.qr.api.dto;

import java.time.LocalDate;

public record QrUsageResponse(
        Long storeId,
        String storeName,
        String usedAt,
        LocalDate usedDate
) {}
