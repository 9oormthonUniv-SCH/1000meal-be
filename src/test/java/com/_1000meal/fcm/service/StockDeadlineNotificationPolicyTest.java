package com._1000meal.fcm.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockDeadlineNotificationPolicyTest {

    private final StockDeadlineNotificationPolicy policy = new StockDeadlineNotificationPolicy();

    @Test
    @DisplayName("remain=31 -> false")
    void remainAboveThreshold() {
        assertFalse(policy.canSend(31));
    }

    @Test
    @DisplayName("remain=30 -> true")
    void remainAtThreshold() {
        assertTrue(policy.canSend(30));
    }

    @Test
    @DisplayName("remain=1 -> true")
    void remainPositive() {
        assertTrue(policy.canSend(1));
    }

    @Test
    @DisplayName("remain=0 -> false")
    void remainZero() {
        assertFalse(policy.canSend(0));
    }
}
