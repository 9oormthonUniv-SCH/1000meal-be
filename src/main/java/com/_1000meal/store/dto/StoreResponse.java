package com._1000meal.store.dto;

import com._1000meal.menu.dto.DailyMenuDto;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponse {

    private Long id;
    private String imageUrl;
    private String name;
    private String address;
    private String phone;
    private String description;
    private String hours;
    private boolean isOpen;
    private int remain;
    private double lat;
    private double lng;
    private DailyMenuDto todayMenu;
}
