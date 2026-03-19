package com._1000meal.menu.service;

import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupDayCapacityRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuGroupStockResetService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MenuGroupStockRepository menuGroupStockRepository;
    private final MenuGroupDayCapacityRepository menuGroupDayCapacityRepository;
    private final DailyMenuRepository dailyMenuRepository;

    @Transactional
    public StockResetSummary resetAllStocksToCapacity() {
        List<MenuGroupStock> stocks = menuGroupStockRepository.findAll();
        LocalDate todayDate = LocalDate.now(KST);
        DayOfWeek today = todayDate.getDayOfWeek();

        int resetCount = 0;
        int skipCount = 0;
        int exceptionCount = 0;
        List<String> exceptionSummaries = new ArrayList<>();

        for (MenuGroupStock stock : stocks) {
            Long groupId = stock.getMenuGroup() != null ? stock.getMenuGroup().getId() : null;
            Long storeId = stock.getMenuGroup() != null && stock.getMenuGroup().getStore() != null
                    ? stock.getMenuGroup().getStore().getId() : null;
            if (storeId != null && dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, todayDate)
                    .filter(dm -> dm.isHoliday()).isPresent()) {
                skipCount++;
                log.debug("[STOCK][RESET][SKIP] groupId={}, storeId={}, reason=store_holiday", groupId, storeId);
                continue;
            }
            int capacity = menuGroupDayCapacityRepository.findByMenuGroupIdAndDayOfWeek(groupId, today)
                    .map(dc -> dc.getCapacity())
                    .filter(c -> c != null && c > 0)
                    .orElseGet(() -> stock.getCapacity() != null ? stock.getCapacity() : 0);

            if (capacity <= 0) {
                skipCount++;
                log.warn("[STOCK][RESET][SKIP] groupId={}, capacity={}, reason=invalid_capacity", groupId, capacity);
                continue;
            }

            try {
                stock.resetTo(capacity);
                resetCount++;
                log.info("[STOCK][RESET][APPLY] groupId={}, stock={}, capacity={}",
                        groupId, stock.getStock(), capacity);
            } catch (Exception e) {
                exceptionCount++;
                String summary = "groupId=" + groupId + ", reason=" + e.getClass().getSimpleName();
                exceptionSummaries.add(summary);
                log.error("[STOCK][RESET][ERROR] {}", summary, e);
            }
        }

        StockResetSummary summary = StockResetSummary.builder()
                .totalCount(stocks.size())
                .resetCount(resetCount)
                .skipCount(skipCount)
                .exceptionCount(exceptionCount)
                .exceptionSummaries(List.copyOf(exceptionSummaries))
                .build();

        log.info("[STOCK][RESET][SUMMARY] total={}, reset={}, skipped={}, exceptions={}",
                summary.getTotalCount(),
                summary.getResetCount(),
                summary.getSkipCount(),
                summary.getExceptionCount());

        return summary;
    }

    @Getter
    @Builder
    public static class StockResetSummary {
        private final int totalCount;
        private final int resetCount;
        private final int skipCount;
        private final int exceptionCount;
        private final List<String> exceptionSummaries;
    }
}
