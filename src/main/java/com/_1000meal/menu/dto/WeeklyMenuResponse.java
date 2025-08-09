package com._1000meal.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class WeeklyMenuResponse {

    private Long storeId;        // 매장 ID
    private LocalDate startDate; // 주간 메뉴 시작일
    private LocalDate endDate;   // 주간 메뉴 종료일
    private List<DailyMenuResponse> dailyMenus; // 하루별 메뉴 목록

    @Getter
    @Builder
    public static class DailyMenuResponse {
        private LocalDate date;       // 날짜
        private DayOfWeek dayOfWeek;  // 요일
        private boolean isOpen;       // 해당 날짜 메뉴 오픈 여부
        private List<String> menuNames; // 메뉴 이름 목록
    }
}
