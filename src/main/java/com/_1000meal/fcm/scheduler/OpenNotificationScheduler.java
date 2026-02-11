package com._1000meal.fcm.scheduler;

import com._1000meal.fcm.service.OpenNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenNotificationScheduler {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final OpenNotificationService openNotificationService;

    @Scheduled(cron = "0 50 7 * * *", zone = "Asia/Seoul")
    public void triggerDailyOpenNotifications() {
        LocalDate today = LocalDate.now(ZONE_ID);
        openNotificationService.sendDailyOpenNotifications(today);
        log.info("[FCM][OPEN] scheduled send completed for {}", today);
    }
}
