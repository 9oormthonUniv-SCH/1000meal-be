package com._1000meal.fcm.scheduler;

import com._1000meal.fcm.service.StockDeadlineNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeadlineNotificationScheduler {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final StockDeadlineNotificationService service;

    @Scheduled(cron = "0 */5 8-10 * * MON-FRI", zone = "Asia/Seoul")
    public void triggerStockDeadlineNotifications() {
        LocalDate today = LocalDate.now(ZONE_ID);
        service.sendStockDeadlineNotifications(today);
        log.info("[FCM][STOCK_DEADLINE] scheduled send completed for {}", today);
    }
}
