package com._1000meal.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DefaultMenuMaterializeResult {
    private LocalDate date;
    private boolean replaced;
    private int itemCount;
    private List<String> menus;
}
