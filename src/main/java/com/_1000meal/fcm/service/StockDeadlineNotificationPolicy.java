package com._1000meal.fcm.service;

import org.springframework.stereotype.Component;

@Component
public class StockDeadlineNotificationPolicy {
    public boolean canSend(int remain) {
        return remain > 0 && remain <= 30;
    }
}
