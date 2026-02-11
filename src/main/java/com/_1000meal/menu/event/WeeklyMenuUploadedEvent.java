package com._1000meal.menu.event;

import java.time.LocalDate;
import java.util.List;

public record WeeklyMenuUploadedEvent(
        Long storeId,
        List<Long> menuGroupIds,
        String weekKey,
        LocalDate weekStart
) {
}
