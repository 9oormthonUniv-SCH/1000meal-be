package com._1000meal.global.scheduler.store;

import com._1000meal.menu.service.MenuGroupStockResetService;
import com._1000meal.holiday.service.HolidayScheduleGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreStockResetScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MenuGroupStockResetService menuGroupStockResetService;
    private final HolidayScheduleGuard holidayScheduleGuard;

    // 월~금 00:00 KST
    @Scheduled(cron = "0 0 0 * * MON-FRI", zone = "Asia/Seoul")
    public void resetStocksAtMidnight() {
        LocalDate today = LocalDate.now(KST);
        if (holidayScheduleGuard.shouldSkip("STOCK_RESET", today)) {
            return;
        }

        log.info("[스케줄러][STOCK_RESET] start");

        MenuGroupStockResetService.StockResetSummary summary =
                menuGroupStockResetService.resetAllStocksToCapacity();

        log.info("[스케줄러][STOCK_RESET] completed. total={}, reset={}, skipped={}, exceptions={}, exceptionSummary={}",
                summary.getTotalCount(),
                summary.getResetCount(),
                summary.getSkipCount(),
                summary.getExceptionCount(),
                summary.getExceptionSummaries());
    }
}
