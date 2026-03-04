package com._1000meal.fcm.service;

import com._1000meal.fcm.domain.NotificationHistory;
import com._1000meal.fcm.domain.NotificationType;
import com._1000meal.fcm.repository.NotificationHistoryRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryServiceTest {

    @Mock
    private NotificationHistoryRepository historyRepository;

    private SimpleMeterRegistry meterRegistry;
    private NotificationHistoryService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new NotificationHistoryService(historyRepository, meterRegistry);
    }

    @Test
    @DisplayName("NULL key는 dedup 비교를 위해 정규화되어 저장된다")
    void normalizeNullableKeysBeforeInsert() {
        LocalDate sentDate = LocalDate.of(2026, 3, 4);
        NotificationHistory saved = NotificationHistory.create(
                NotificationType.OPEN, 10L, 20L, 0L, sentDate, ""
        );
        when(historyRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(NotificationHistory.class)))
                .thenReturn(saved);

        boolean marked = service.tryMarkSent(NotificationType.OPEN, 10L, 20L, null, sentDate, null);

        assertTrue(marked);
        ArgumentCaptor<NotificationHistory> captor = ArgumentCaptor.forClass(NotificationHistory.class);
        verify(historyRepository).saveAndFlush(captor.capture());
        NotificationHistory history = captor.getValue();
        assertEquals(0L, history.getMenuGroupId());
        assertEquals("", history.getWeekKey());
        assertEquals(1.0, meterRegistry.get("notification.dedup.acquired")
                .tag("type", "OPEN").counter().count());
    }

    @Test
    @DisplayName("유니크 충돌 시 false를 반환하고 skip metric이 증가한다")
    void returnFalseWhenDuplicateKey() {
        LocalDate sentDate = LocalDate.of(2026, 3, 4);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(historyRepository)
                .saveAndFlush(org.mockito.ArgumentMatchers.any(NotificationHistory.class));

        boolean marked = service.tryMarkSent(
                NotificationType.STOCK_DEADLINE, 1L, 2L, 3L, sentDate, null
        );

        assertFalse(marked);
        assertEquals(1.0, meterRegistry.get("notification.dedup.skipped")
                .tag("type", "STOCK_DEADLINE").counter().count());
    }
}
