package com._1000meal.menu.dto;

import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.Menu;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
public class DailyMenuDto {

    private Long id;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private boolean isOpen;
    private List<String> menus;

}
