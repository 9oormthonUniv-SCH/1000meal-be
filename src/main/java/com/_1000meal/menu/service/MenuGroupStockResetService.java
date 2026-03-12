package com._1000meal.menu.service;

import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuGroupStockResetService {

    private final MenuGroupStockRepository menuGroupStockRepository;

    @Transactional
    public StockResetSummary resetAllStocksToCapacity() {
        List<MenuGroupStock> stocks = menuGroupStockRepository.findAll();

        int resetCount = 0;
        int skipCount = 0;
        int exceptionCount = 0;
        List<String> exceptionSummaries = new ArrayList<>();

        for (MenuGroupStock stock : stocks) {
            Long groupId = stock.getMenuGroup() != null ? stock.getMenuGroup().getId() : null;
            Integer capacity = stock.getCapacity();

            if (capacity == null || capacity <= 0) {
                skipCount++;
                log.warn("[STOCK][RESET][SKIP] groupId={}, capacity={}, reason=invalid_capacity", groupId, capacity);
                continue;
            }

            try {
                stock.resetDaily();
                resetCount++;
                log.info("[STOCK][RESET][APPLY] groupId={}, stock={}, capacity={}",
                        groupId, stock.getStock(), stock.getCapacity());
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
