package com._1000meal.qr.api.dto;

public record TodayQrUsageResponse(
        boolean used,
        Long storeId,
        String storeName,
        String usedAt,
        String usedDate
) {}
