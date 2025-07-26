package com._1000meal.store.dto;

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
}
