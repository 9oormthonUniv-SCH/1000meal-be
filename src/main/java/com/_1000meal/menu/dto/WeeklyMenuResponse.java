package com._1000meal.menu.dto;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class WeeklyMenuResponse {
    private Long storeId;
    private List<DailyMenuResponse> dailyMenus;

    @Data
    @Builder
    public static class DailyMenuResponse {
        private LocalDate date;
        private DayOfWeek dayOfWeek;
        private boolean isOpen;
        private List<String> menuNames;
    }
}