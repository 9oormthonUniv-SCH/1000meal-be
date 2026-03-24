package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.NotificationHistory;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.MeterRegistry;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
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

            if (isDuplicateKeyViolation(e)) {
                meterRegistry.counter("notification.dedup.skipped", "type", type.name()).increment();
                log.info("[FCM][DEDUP] skip type={}, accountId={}, storeId={}, menuGroupId={}, sentDate={}, weekKey={}",
                        type, accountId, storeId, normalizedMenuGroupId, sentDate, normalizedWeekKey);
                return false;
            }
            throw e;
        } catch (RuntimeException e) {
            // 원인 체인을 스캔해서 duplicate로 확인되면 동일하게 skip
            if (isDuplicateKeyViolation(e)) {
                meterRegistry.counter("notification.dedup.skipped", "type", type.name()).increment();
                log.info("[FCM][DEDUP] skip type={}, accountId={}, storeId={}, menuGroupId={}, sentDate={}, weekKey={}",
                        type, accountId, storeId, normalizedMenuGroupId, sentDate, normalizedWeekKey);
                return false;
            }
            // duplicate로 확인되지 않으면 실제 장애!
            throw e;
        }
    }

    static boolean isDuplicateKeyViolation(Throwable e) {
        Throwable current = e;

        while (current != null) {
            // Hibernate가 JDBC 예외를 감싸는 대표 케이스
            if (current instanceof ConstraintViolationException cve) {
                SQLException sqlEx = cve.getSQLException();
                if (isMysqlDuplicateKey(sqlEx)) {
                    return true;
                }
            }

            // JDBC 예외가 직접 체인에 있는 케이스
            if (current instanceof SQLException sqlEx) {
                if (isMysqlDuplicateKey(sqlEx)) {
                    return true;
                }
            }

            current = current.getCause();
        }

        return false;
    }

    private static boolean isMysqlDuplicateKey(SQLException sqlEx) {
        if (sqlEx == null) {
            return false;
        }

        if (sqlEx.getErrorCode() == 1062) {
            return true;
        }
        return "23000".equals(sqlEx.getSQLState()) && containsDuplicateEntry(sqlEx);
    }

    private static boolean containsDuplicateEntry(SQLException sqlEx) {
        String message = sqlEx.getMessage();
        return message != null && message.contains("Duplicate entry");
    }
}
