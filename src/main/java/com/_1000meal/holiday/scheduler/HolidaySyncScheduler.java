package com._1000meal.holiday.scheduler;

import com._1000meal.holiday.service.HolidaySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 연초에 1년치 공휴일 정보를 동기화하는 스케줄러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HolidaySyncScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final HolidaySyncService holidaySyncService;

    @Scheduled(cron = "0 0 6 1 1 *", zone = "Asia/Seoul")
    public void syncHolidays() {
        int year = LocalDate.now(KST).getYear();
        log.info("[HOLIDAY-SYNC] scheduled start year={}", year);
        holidaySyncService.syncYear(year);
    }
}

