package com._1000meal.menu.service;

import com._1000meal.fcm.service.WeeklyMenuNotificationStateService;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.dto.GroupDailyMenuResponse;
import com._1000meal.menu.dto.MenuUpdateRequest;
import com._1000meal.menu.event.WeeklyMenuChangedEvent;
import com._1000meal.menu.event.WeeklyMenuUploadedEvent;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceNotificationTriggerTest {

    @Mock MenuGroupRepository menuGroupRepository;
    @Mock GroupDailyMenuRepository groupDailyMenuRepository;
    @Mock WeeklyMenuNotificationStateService weeklyMenuNotificationStateService;
    @Mock ApplicationEventPublisher eventPublisher;

    @Spy
    @InjectMocks
    MenuGroupService service;

    @Test
    @DisplayName("즉시 업로드 알림 대상이면 업로드 이벤트를 발행하고 상태를 SENT로 기록한다")
    void updateMenusInGroupForStore_immediateUploadAlert() {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate date = LocalDate.of(2026, 3, 18);
        LocalDate weekStart = LocalDate.of(2026, 3, 16);
        String weekKey = "2026-03-16";

        stubAuthorizedGroup(storeId, groupId);

        // 해당 날짜 메뉴 없다고 가정
        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, date)).thenReturn(Optional.empty());
        // 저장된 객체를 그대로 돌려줌
        when(groupDailyMenuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 즉시 전송 결과가 true라고 가정했을 때, 동작하는지 확인
        doReturn(true).when(service).shouldSendImmediateWeeklyUploadAlert(storeId, groupId, date);

        GroupDailyMenuResponse response = service.updateMenusInGroupForStore(
                storeId, groupId, date, new MenuUpdateRequest(List.of("김밥", "국수"))
        );

        assertNotNull(response);
        assertEquals(groupId, response.getGroupId());

        ArgumentCaptor<WeeklyMenuUploadedEvent> captor =
                ArgumentCaptor.forClass(WeeklyMenuUploadedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        WeeklyMenuUploadedEvent event = captor.getValue();

        assertEquals(storeId, event.storeId());
        assertEquals(List.of(groupId), event.menuGroupIds());
        assertEquals(weekKey, event.weekKey());
        assertEquals(weekStart, event.weekStart());

        verify(weeklyMenuNotificationStateService).markSent(storeId, groupId, weekKey);
        verify(service, never()).shouldSendMenuChangeAlert(storeId, groupId, date);
    }

    @Test
    @DisplayName("이미 발송된 주간 메뉴를 수정하면 메뉴 변경 이벤트만 발행한다")
    void updateMenusInGroupForStore_menuChangeAlert() {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate date = LocalDate.of(2026, 3, 18);
        LocalDate weekStart = LocalDate.of(2026, 3, 16);
        String weekKey = "2026-03-16";

        stubAuthorizedGroup(storeId, groupId);
        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, date)).thenReturn(Optional.empty());
        when(groupDailyMenuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        doReturn(false).when(service).shouldSendImmediateWeeklyUploadAlert(storeId, groupId, date);
        doReturn(true).when(service).shouldSendMenuChangeAlert(storeId, groupId, date);

        GroupDailyMenuResponse response = service.updateMenusInGroupForStore(
                storeId, groupId, date, new MenuUpdateRequest(List.of("김밥", "국수"))
        );

        assertNotNull(response);
        assertEquals(groupId, response.getGroupId());

        ArgumentCaptor<WeeklyMenuChangedEvent> captor =
                ArgumentCaptor.forClass(WeeklyMenuChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        WeeklyMenuChangedEvent event = captor.getValue();

        assertEquals(storeId, event.storeId());
        assertEquals(List.of(groupId), event.menuGroupIds());
        assertEquals(weekKey, event.weekKey());
        assertEquals(weekStart, event.weekStart());

        verify(weeklyMenuNotificationStateService, never()).markSent(storeId, groupId, weekKey);
    }

    private void stubAuthorizedGroup(Long storeId, Long groupId) {
        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(groupId);
        when(group.getName()).thenReturn("기본 메뉴");
        when(menuGroupRepository.findByIdAndStoreId(groupId, storeId)).thenReturn(Optional.of(group));
    }
}
