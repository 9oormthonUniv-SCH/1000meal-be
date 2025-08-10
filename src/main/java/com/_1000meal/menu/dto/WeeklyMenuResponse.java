package com._1000meal.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class WeeklyMenuResponse {

    private Long storeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyMenuDto> dailyMenus;
}
