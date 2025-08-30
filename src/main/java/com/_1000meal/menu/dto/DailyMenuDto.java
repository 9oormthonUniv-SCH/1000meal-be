package com._1000meal.menu.dto;



import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DailyMenuDto {

    private Long id;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private boolean isOpen;
    private Integer stock;
    private List<String> menus;

}
