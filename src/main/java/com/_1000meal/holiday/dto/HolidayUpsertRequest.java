package com._1000meal.holiday.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayUpsertRequest(
        @NotNull LocalDate date,
        @NotBlank String name
) {
}
