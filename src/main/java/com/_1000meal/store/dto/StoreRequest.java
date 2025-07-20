package com._1000meal.store.dto;

import lombok.*;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRequest {
    private String name;
    private String address;
    private String phone;
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;
    private int remain;
    private String hours;
    private double lat;
    private double lng;
}
