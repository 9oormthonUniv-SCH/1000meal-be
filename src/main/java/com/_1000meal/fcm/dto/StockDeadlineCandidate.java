package com._1000meal.fcm.dto;

public record StockDeadlineCandidate(
        Long accountId,
        Long storeId,
        String storeName,
        String storeImageUrl,
        Long menuGroupId,
        String menuGroupName,
        Integer menuGroupSortOrder,
        Integer groupStock,
        Integer storeRemain
) {
}
