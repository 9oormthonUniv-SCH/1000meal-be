package com._1000meal.fcm.service;

import com._1000meal.fcm.dto.OpenNotificationTarget;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class DefaultOpenNotificationStorePolicy implements OpenNotificationStorePolicy {
    @Override
    public boolean canSend(OpenNotificationTarget target, boolean hasTodayMenu, LocalDate date) {
        if (isWeekend(date)) {
            return false;
        }
        if (!target.storeIsOpen()) {
            return false;
        }
        return hasTodayMenu;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
