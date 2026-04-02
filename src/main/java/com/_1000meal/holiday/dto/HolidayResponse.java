package com._1000meal.holiday.dto;

import com._1000meal.holiday.domain.Holiday;

import java.time.LocalDate;

public record HolidayResponse(
        Long id,
        LocalDate date,
        String name
) {
    public static HolidayResponse from(Holiday holiday) {
        return new HolidayResponse(
                holiday.getId(),
                holiday.getDate(),
                holiday.getName()
        );
    }
}
