package com._1000meal.favorite.dto;

public record FavoriteStoreResponse(
        Long storeId,
        String storeName,
        String storeImageUrl,
        boolean storeIsOpen
) {
}
