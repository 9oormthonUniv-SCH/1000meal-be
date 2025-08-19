package com._1000meal.global.scheduler.store;

import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoreScheduler {

    private final StoreRepository storeRepository;

    // 매일 오전 8시 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    @Transactional
    public void autoOpenStores() {
        int updatedCount = storeRepository.bulkUpdateStoreStatus(true);
        log.info("[스케줄러] {}개 매장 영업 상태 ON으로 변경", updatedCount);
    }
}
