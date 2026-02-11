package com._1000meal.menu.event;

import java.time.LocalDate;

public record WeeklyMenuUploadedEvent(
        Long storeId,
        String weekKey,
        LocalDate weekStart
) {
}
