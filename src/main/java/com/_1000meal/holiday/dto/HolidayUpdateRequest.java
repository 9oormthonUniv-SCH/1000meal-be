package com._1000meal.holiday.dto;

import jakarta.validation.constraints.NotBlank;

public record HolidayUpdateRequest(
        @NotBlank String name
) {
}
