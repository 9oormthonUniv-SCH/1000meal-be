package com._1000meal.fcm.dto;

public record OpenNotificationTarget(
        Long accountId,
        Long storeId,
        String storeName,
        String storeImageUrl,
        boolean storeIsOpen
) {}
