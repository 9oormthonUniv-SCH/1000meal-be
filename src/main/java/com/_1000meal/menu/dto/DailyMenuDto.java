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
    private boolean isHoliday;
    private Integer stock;
    @Builder.Default
    private List<String> menus = List.of();
    @Builder.Default
    private List<MenuGroupResponseDto> menuGroups = List.of();

}
