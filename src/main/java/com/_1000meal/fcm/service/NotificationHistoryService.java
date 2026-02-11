package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.NotificationHistory;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class NotificationHistoryService {

    private final NotificationHistoryRepository historyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryMarkSent(NotificationType type, Long accountId, Long storeId, LocalDate sentDate) {
        try {
            historyRepository.saveAndFlush(NotificationHistory.create(type, accountId, storeId, sentDate));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
