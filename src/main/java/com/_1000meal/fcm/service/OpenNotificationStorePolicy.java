package com._1000meal.fcm.service;

import com._1000meal.fcm.dto.OpenNotificationTarget;

import java.time.LocalDate;

public interface OpenNotificationStorePolicy {
    boolean canSend(OpenNotificationTarget target, boolean hasTodayMenu, LocalDate date);
}
