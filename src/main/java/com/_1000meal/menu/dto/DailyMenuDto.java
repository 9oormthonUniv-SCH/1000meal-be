package com._1000meal.menu.dto;



import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
public class DailyMenuDto {

    private Long id;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private boolean isOpen;
    private Integer stock;
    private List<String> menus;

}
