package com._1000meal.global.scheduler.store;

import com._1000meal.menu.service.MenuGroupStockResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreStockResetScheduler {

    private final MenuGroupStockResetService menuGroupStockResetService;

    // 월~금 00:00 KST
    @Scheduled(cron = "0 0 0 * * MON-FRI", zone = "Asia/Seoul")
    public void resetStocksAtMidnight() {
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
