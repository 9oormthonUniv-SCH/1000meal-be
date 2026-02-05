package com._1000meal.menu.dto;

import com._1000meal.menu.domain.DefaultGroupMenu;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DefaultMenuResponse {
    private Long id;
    private String menuName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    public static DefaultMenuResponse from(DefaultGroupMenu rule, LocalDate today) {
        boolean active = rule.isActive();

        return DefaultMenuResponse.builder()
                .id(rule.getId())
                .menuName(rule.getMenuName())
                .startDate(rule.getStartDate())
                .endDate(rule.getEndDate())
                .active(active)
                .build();
    }
}
