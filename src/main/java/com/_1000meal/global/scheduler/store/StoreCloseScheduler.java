package com._1000meal.global.scheduler.store;

import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.repository.StoreRepository;
import com._1000meal.qr.roster.RosterExportJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreCloseScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StoreRepository storeRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final MenuGroupStockRepository menuGroupStockRepository;
    private final RosterExportJob rosterExportJob;

    /**
     * 매장 영업 종료 스케줄러
     * - 모든 Store.isOpen 을 false 로 설정
     * - 오늘 날짜 DailyMenu.isOpen 을 false 로 설정
     * - 매장 기준 MenuGroupStock.stock 을 0 으로 설정
     */
    @Scheduled(cron = "0 0 12 * * MON-FRI", zone = "Asia/Seoul")
    @Transactional   
    public void closeAllStores() {
        LocalDate today = LocalDate.now(KST);
        DayOfWeek dow = today.getDayOfWeek();

        log.info("[스케줄러][STORE_CLOSE] start. date={}, dayOfWeek={}", today, dow);

        // 매장 Close
        int updatedStores = storeRepository.bulkUpdateStoreStatus(false);
        log.info("[스케줄러][STORE_CLOSE] bulkUpdateStoreStatus(false) updated={}", updatedStores);

        // DailyMenu Close
        List<Long> storeIds = storeRepository.findAllStoreIds();
        if (storeIds.isEmpty()) {
            log.info("[스케줄러][STORE_CLOSE] no stores found. skip DailyMenu update.");
            return;
        }

        List<DailyMenu> dailyMenus = dailyMenuRepository.findByStoreIdInAndDate(storeIds, today);
        for (DailyMenu dm : dailyMenus) {
            if (dm.isOpen()) {
                dm.toggleIsOpen();
            }
        }

        int updatedStocks = menuGroupStockRepository.setStockZeroByStoreIds(storeIds);

        log.info("[스케줄러][STORE_CLOSE] date={}, dailyMenusUpdated={}, menuGroupStocksUpdated={}",
                today, dailyMenus.size(), updatedStocks);

        try {
            rosterExportJob.runOnce(today);
            log.info("[스케줄러][STORE_CLOSE] roster export+sync completed. date={}", today);
        } catch (Exception e) {
            log.error("[스케줄러][STORE_CLOSE] roster export+sync failed. date={}, error={}", today, e.getMessage(), e);
        }
    }
}

