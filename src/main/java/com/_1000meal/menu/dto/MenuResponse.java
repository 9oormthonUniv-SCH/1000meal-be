package com._1000meal.menu.dto;


import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private Long id;
    private Long storeId;
    private LocalDate date;
    private String imageUrl;
}