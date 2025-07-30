package com._1000meal.store.dto;

import com._1000meal.store.domain.Store;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String description;
    private String hours;
    private boolean isOpen;
    private int remain;
    private double lat;
    private double lng;
    private List<String> menu;  // 메뉴 이름 목록 (imageUrl을 가공한 리스트)

    //create, getAll
    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .description(store.getDescription())
                .hours(store.getHours())
                .remain(store.getRemain())
                .lat(store.getLat())
                .lng(store.getLng())
                .isOpen(store.isOpen())
                .build();
    }

}
