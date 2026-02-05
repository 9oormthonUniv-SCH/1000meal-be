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
        boolean active = false;
        if (today != null && rule.getStartDate() != null) {
            boolean started = !today.isBefore(rule.getStartDate());
            boolean notEnded = rule.getEndDate() == null || !rule.getEndDate().isBefore(today);
            active = started && notEnded;
        }

        return DefaultMenuResponse.builder()
                .id(rule.getId())
                .menuName(rule.getMenuName())
                .startDate(rule.getStartDate())
                .endDate(rule.getEndDate())
                .active(active)
                .build();
    }
}
