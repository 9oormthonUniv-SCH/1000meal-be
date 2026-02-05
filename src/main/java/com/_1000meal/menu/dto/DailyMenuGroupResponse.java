package com._1000meal.menu.dto;

import com._1000meal.menu.domain.DailyMenu;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DailyMenuGroupResponse {
    private Long id;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private boolean isOpen;
    private boolean holiday;
    private Integer totalStock;
    private List<MenuGroupResponse> groups;

    public static DailyMenuGroupResponse from(DailyMenu dailyMenu, List<MenuGroupResponse> groups) {
        return DailyMenuGroupResponse.builder()
                .id(dailyMenu.getId())
                .date(dailyMenu.getDate())
                .dayOfWeek(dailyMenu.getDate() != null ? dailyMenu.getDate().getDayOfWeek() : dailyMenu.getDayOfWeek())
                .isOpen(dailyMenu.isOpen())
                .holiday(dailyMenu.isHoliday())
                .totalStock(null)
                .groups(groups)
                .build();
    }

    public static DailyMenuGroupResponse skeleton(LocalDate date) {
        return DailyMenuGroupResponse.builder()
                .id(null)
                .date(date)
                .dayOfWeek(date.getDayOfWeek())
                .isOpen(false)
                .holiday(false)
                .totalStock(null)
                .groups(List.of())
                .build();
    }

    public static DailyMenuGroupResponse storeBased(
            DailyMenu dailyMenu,
            LocalDate date,
            List<MenuGroupResponse> groups,
            Integer totalStock,
            boolean isOpen,
            boolean holiday
    ) {
        return DailyMenuGroupResponse.builder()
                .id(dailyMenu != null ? dailyMenu.getId() : null)
                .date(date)
                .dayOfWeek(date.getDayOfWeek())
                .isOpen(isOpen)
                .holiday(holiday)
                .totalStock(totalStock)
                .groups(groups)
                .build();
    }
}
