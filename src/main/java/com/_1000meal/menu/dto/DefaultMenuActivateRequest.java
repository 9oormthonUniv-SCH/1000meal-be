package com._1000meal.menu.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DefaultMenuActivateRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
