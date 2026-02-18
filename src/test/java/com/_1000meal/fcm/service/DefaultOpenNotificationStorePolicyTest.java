package com._1000meal.fcm.service;

import com._1000meal.fcm.dto.OpenNotificationTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultOpenNotificationStorePolicyTest {

    private final DefaultOpenNotificationStorePolicy policy = new DefaultOpenNotificationStorePolicy();

    @Test
    @DisplayName("평일 + 오픈 + 메뉴있음이면 true")
    void weekdayOpenWithMenu() {
        LocalDate weekday = LocalDate.of(2026, 2, 11);
        OpenNotificationTarget target = new OpenNotificationTarget(1L, 10L, "매장", "img", true);

        assertTrue(policy.canSend(target, true, weekday));
    }

    @Test
    @DisplayName("주말이면 false")
    void weekendFalse() {
        LocalDate saturday = LocalDate.of(2026, 2, 7);
        OpenNotificationTarget target = new OpenNotificationTarget(1L, 10L, "매장", "img", true);

        assertFalse(policy.canSend(target, true, saturday));
    }

    @Test
    @DisplayName("매장 미오픈이면 false")
    void storeClosedFalse() {
        LocalDate weekday = LocalDate.of(2026, 2, 11);
        OpenNotificationTarget target = new OpenNotificationTarget(1L, 10L, "매장", "img", false);

        assertFalse(policy.canSend(target, true, weekday));
    }

    @Test
    @DisplayName("오늘 메뉴 없으면 false")
    void noMenuFalse() {
        LocalDate weekday = LocalDate.of(2026, 2, 11);
        OpenNotificationTarget target = new OpenNotificationTarget(1L, 10L, "매장", "img", true);

        assertFalse(policy.canSend(target, false, weekday));
    }
}
