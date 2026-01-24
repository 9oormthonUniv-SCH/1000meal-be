package com._1000meal.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class WeeklyMenuWithGroupsResponse {
    private Long storeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyMenuGroupResponse> dailyMenus;
}
