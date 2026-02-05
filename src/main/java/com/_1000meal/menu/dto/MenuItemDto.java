package com._1000meal.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuItemDto {
    private String name;
    private boolean pinned;
}
