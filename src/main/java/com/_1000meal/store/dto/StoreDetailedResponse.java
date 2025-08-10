package com._1000meal.store.dto;

import com._1000meal.menu.dto.WeeklyMenuResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class StoreDetailedResponse {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isOpen;
    private int remain;
    private String hours;
    private double lat;
    private double lng;

    private WeeklyMenuResponse weeklyMenuResponse;

}
