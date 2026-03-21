package com._1000meal.fcm.scheduler;

import com._1000meal.menu.event.WeeklyMenuUploadedEvent;
import com._1000meal.menu.service.MenuGroupService;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyMenuNotificationSchedulerTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private MenuGroupService menuGroupService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WeeklyMenuNotificationScheduler scheduler;

    @Test
    @DisplayName("일요일 18시 배치 시 다음 주 완료 그룹이 있는 매장만 이벤트를 발행한다")
    void notifyCompletedWeeklyMenus_publishEventOnlyForCompletedGroups() {
        LocalDate sunday = LocalDate.of(2026, 3, 15);
        LocalDate nextMonday = LocalDate.of(2026, 3, 16);

        when(storeRepository.findAllStoreIds()).thenReturn(List.of(1L, 2L));
        when(menuGroupService.findCompletedWeeklyMenuGroupIds(1L, nextMonday))
                .thenReturn(List.of(10L, 11L));
        when(menuGroupService.findCompletedWeeklyMenuGroupIds(2L, nextMonday))
                .thenReturn(List.of());

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(sunday);

            scheduler.notifyCompletedWeeklyMenus();
        }

        ArgumentCaptor<WeeklyMenuUploadedEvent> captor =
                ArgumentCaptor.forClass(WeeklyMenuUploadedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        verify(menuGroupService).findCompletedWeeklyMenuGroupIds(1L, nextMonday);
        verify(menuGroupService).findCompletedWeeklyMenuGroupIds(2L, nextMonday);

        WeeklyMenuUploadedEvent event = captor.getValue();
        assertEquals(1L, event.storeId());
        assertEquals(List.of(10L, 11L), event.menuGroupIds());
        assertEquals("2026-03-16", event.weekKey());
        assertEquals(nextMonday, event.weekStart());
    }

    @Test
    @DisplayName("완료된 그룹이 없으면 이벤트를 발행하지 않는다")
    void notifyCompletedWeeklyMenus_skipWhenNoCompletedGroups() {
        LocalDate sunday = LocalDate.of(2026, 3, 15);
        LocalDate nextMonday = LocalDate.of(2026, 3, 16);

        when(storeRepository.findAllStoreIds()).thenReturn(List.of(1L));
        when(menuGroupService.findCompletedWeeklyMenuGroupIds(1L, nextMonday))
                .thenReturn(List.of());

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(sunday);

            scheduler.notifyCompletedWeeklyMenus();
        }

        verify(menuGroupService).findCompletedWeeklyMenuGroupIds(1L, nextMonday);
        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("매장이 없으면 메뉴 완료 여부 조회 없이 종료한다")
    void notifyCompletedWeeklyMenus_skipWhenNoStores() {
        LocalDate sunday = LocalDate.of(2026, 3, 15);

        when(storeRepository.findAllStoreIds()).thenReturn(List.of());

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(sunday);

            scheduler.notifyCompletedWeeklyMenus();
        }

        verifyNoInteractions(menuGroupService, eventPublisher);
    }
}
