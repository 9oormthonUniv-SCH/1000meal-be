package com._1000meal.qr.api.dto;

public record QrStoreResponse(
        Long storeId,
        String storeName,
        Long menuGroupId,
        String menuGroupName,
        String qrToken,
        boolean isActive
) {
}
