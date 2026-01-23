package com._1000meal.menu.dto;

import com._1000meal.menu.domain.DailyMenu;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DailyMenuWithGroupsDto {
    private Long id;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private boolean isOpen;
    private boolean isHoliday;
    private Integer totalStock;
    private List<MenuGroupDto> groups;

    public static DailyMenuWithGroupsDto from(DailyMenu dailyMenu, List<MenuGroupDto> groups) {
        int totalStock = groups.stream()
                .mapToInt(g -> g.getStock() != null ? g.getStock() : 0)
                .sum();

        return DailyMenuWithGroupsDto.builder()
                .id(dailyMenu.getId())
                .date(dailyMenu.getDate())
                .dayOfWeek(dailyMenu.getDate() != null ? dailyMenu.getDate().getDayOfWeek() : dailyMenu.getDayOfWeek())
                .isOpen(dailyMenu.isOpen())
                .isHoliday(dailyMenu.isHoliday())
                .totalStock(totalStock)
                .groups(groups)
                .build();
    }
}
