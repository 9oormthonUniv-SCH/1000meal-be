package com._1000meal.store.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRequest {
    private Long storeId;
    private LocalDate date;
    private String imageUrl;
}
