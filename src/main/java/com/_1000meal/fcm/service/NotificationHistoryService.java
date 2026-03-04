package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.NotificationHistory;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHistoryService {

    private final NotificationHistoryRepository historyRepository;
    private final MeterRegistry meterRegistry;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryMarkSent(
            NotificationType type,
            Long accountId,
            Long storeId,
            Long menuGroupId,
            LocalDate sentDate,
            String weekKey
    ) {
        Long normalizedMenuGroupId = menuGroupId == null ? 0L : menuGroupId;
        String normalizedWeekKey = weekKey == null ? "" : weekKey;

        try {
            historyRepository.saveAndFlush(NotificationHistory.create(
                    type, accountId, storeId, normalizedMenuGroupId, sentDate, normalizedWeekKey
            ));
            meterRegistry.counter("notification.dedup.acquired", "type", type.name()).increment();
            log.info("[FCM][DEDUP] acquired type={}, accountId={}, storeId={}, menuGroupId={}, sentDate={}, weekKey={}",
                    type, accountId, storeId, normalizedMenuGroupId, sentDate, normalizedWeekKey);
            return true;
        } catch (DataIntegrityViolationException e) {
            meterRegistry.counter("notification.dedup.skipped", "type", type.name()).increment();
            log.info("[FCM][DEDUP] skip type={}, accountId={}, storeId={}, menuGroupId={}, sentDate={}, weekKey={}",
                    type, accountId, storeId, normalizedMenuGroupId, sentDate, normalizedWeekKey);
            return false;
        }
    }
}
