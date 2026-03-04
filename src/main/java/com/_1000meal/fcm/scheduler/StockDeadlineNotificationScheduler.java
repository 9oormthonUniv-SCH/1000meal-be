package com._1000meal.fcm.scheduler;

import com._1000meal.fcm.service.StockDeadlineNotificationService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeadlineNotificationScheduler {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final StockDeadlineNotificationService service;
    private final MeterRegistry meterRegistry;

    @Scheduled(cron = "0 */5 8-10 * * MON-FRI", zone = "Asia/Seoul")
    public void triggerStockDeadlineNotifications() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        LocalDate today = LocalDate.now(ZONE_ID);
        meterRegistry.counter("notification.stock_deadline.scheduler.tick").increment();
        log.info("[FCM][STOCK_DEADLINE] scheduler tick. now={}, zone={}, window=08:00-10:59/5m",
                now, ZONE_ID);
        service.sendStockDeadlineNotifications(today);
        log.info("[FCM][STOCK_DEADLINE] scheduled send completed for {}", today);
    }
}
