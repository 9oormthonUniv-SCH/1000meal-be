package com._1000meal.favorite.dto;

import com._1000meal.store.domain.Store;
import lombok.Getter;

@Getter
public class FavoriteStoreItemResponse {
    private final Long storeId;
    private final String name;
    private final String address;
    private final String imageUrl;
    private final boolean open;
    private final Integer remain;

    private FavoriteStoreItemResponse(Long storeId, String name, String address, String imageUrl, boolean open, Integer remain) {
        this.storeId = storeId;
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.open = open;
        this.remain = remain;
    }

    public static FavoriteStoreItemResponse from(Store store) {
        return new FavoriteStoreItemResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getImageUrl(),
                store.isOpen(),     // store 엔티티 getter에 맞게 조정
                store.getRemain()
        );
    }
}