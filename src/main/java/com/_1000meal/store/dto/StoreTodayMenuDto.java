package com._1000meal.store.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
public class StoreTodayMenuDto {
    private Long id;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private boolean isOpen;
    private boolean isHoliday;
    @Builder.Default
    private List<StoreTodayMenuGroupDto> menuGroups = List.of();
}
