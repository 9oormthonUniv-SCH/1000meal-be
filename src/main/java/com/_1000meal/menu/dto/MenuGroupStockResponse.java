package com._1000meal.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MenuGroupStockResponse {
    private Long groupId;
    private Integer stock;
}
