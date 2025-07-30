package com._1000meal.menu.dto;
import lombok.*;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Data
public class WeeklyMenuRequest {
    private Long storeId;
    private List<DailyMenuRequest> dailyMenus;

    @Data
    public static class DailyMenuRequest {
        private LocalDate date;
        private DayOfWeek dayOfWeek;
        private boolean isOpen;
        private List<String> menuNames; // 클라이언트는 List<String>으로 보냄
    }
}

