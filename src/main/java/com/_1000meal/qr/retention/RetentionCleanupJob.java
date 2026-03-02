package com._1000meal.qr.retention;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetentionCleanupJob {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RetentionCleanupService retentionCleanupService;

    @Scheduled(cron = "0 5 10 * * *", zone = "Asia/Seoul")
    public void runDaily() {
        LocalDate cutoffDate = LocalDate.now(KST).minusDays(7);
        log.info("Retention cleanup job started: cutoffDate={}", cutoffDate);
        retentionCleanupService.cleanup(cutoffDate);
    }
}
