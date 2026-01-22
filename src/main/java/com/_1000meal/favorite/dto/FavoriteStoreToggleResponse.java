package com._1000meal.favorite.dto;

import lombok.Getter;

@Getter
public class FavoriteStoreToggleResponse {
    private final Long storeId;
    private final boolean favorite;

    private FavoriteStoreToggleResponse(Long storeId, boolean favorite) {
        this.storeId = storeId;
        this.favorite = favorite;
    }

    public static FavoriteStoreToggleResponse of(Long storeId, boolean favorite) {
        return new FavoriteStoreToggleResponse(storeId, favorite);
    }
}