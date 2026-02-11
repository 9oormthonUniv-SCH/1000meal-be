package com._1000meal.fcm.service;

import com._1000meal.favorite.repository.FavoriteStoreRepository;
import com._1000meal.fcm.dto.StockDeadlineCandidate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockDeadlineNotificationServiceTest {

    @Mock
    FavoriteStoreRepository favoriteStoreRepository;

    @Mock
    StockDeadlineNotificationPolicy policy;

    @Mock
    NotificationHistoryService historyService;

    @Mock
    FcmPushService fcmPushService;

    @InjectMocks
    StockDeadlineNotificationService service;

    @Test
    @DisplayName("그룹 재고가 있으면 그룹별로 발송한다")
    void sendsPerGroupWhenStockExists() {
        LocalDate date = LocalDate.of(2026, 2, 11);
        StockDeadlineCandidate g1 = new StockDeadlineCandidate(
                1L, 4L, "향설2관", "img", 10L, "A", 1, 30, 99
        );
        StockDeadlineCandidate g2 = new StockDeadlineCandidate(
                1L, 4L, "향설2관", "img", 11L, "B", 2, 20, 99
        );

        when(favoriteStoreRepository.findStockDeadlineCandidates()).thenReturn(List.of(g1, g2));
        when(policy.canSend(anyInt())).thenReturn(true);
        when(historyService.tryMarkSent(any(), any(), any(), any(), any())).thenReturn(true);

        service.sendStockDeadlineNotifications(date);

        verify(fcmPushService).sendStockDeadlineNotification(
                1L, 4L, "향설2관", "img", 10L, "A", 30
        );
        verify(fcmPushService).sendStockDeadlineNotification(
                1L, 4L, "향설2관", "img", 11L, "B", 20
        );
    }
}
