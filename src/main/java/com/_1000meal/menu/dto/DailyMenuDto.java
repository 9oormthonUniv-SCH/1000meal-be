package com._1000meal.menu.dto;

import com._1000meal.menu.domain.DailyMenu;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class DailyMenuDto {
    private Long id;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private boolean isOpen;
    private List<String> menus;

    public static DailyMenuDto from(DailyMenu entity) {
        return DailyMenuDto.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .dayOfWeek(entity.getDayOfWeek())
                .isOpen(entity.isOpen())
                .menus(splitMenuTexts(entity.getMenuTexts()))
                .build();
    }

    private static List<String> splitMenuTexts(String menuTexts) {
        if (menuTexts == null || menuTexts.trim().isEmpty()) return List.of();
        return Arrays.stream(menuTexts.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public static DailyMenuDto empty(DayOfWeek dow) {
        return DailyMenuDto.builder()
                .id(null)
                .date(null)
                .dayOfWeek(dow)
                .isOpen(false)
                .menus(List.of())
                .build();
    }
}
