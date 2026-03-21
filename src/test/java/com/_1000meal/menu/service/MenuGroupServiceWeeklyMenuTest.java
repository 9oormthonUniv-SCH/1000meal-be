package com._1000meal.menu.service;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.fcm.domain.WeeklyMenuNotificationStatus;
import com._1000meal.fcm.service.WeeklyMenuNotificationStateService;
import com._1000meal.menu.domain.GroupDailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.repository.DefaultGroupMenuRepository;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZoneId;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceWeeklyMenuTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock MenuGroupRepository menuGroupRepository;
    @Mock MenuGroupStockRepository stockRepository;
    @Mock DailyMenuRepository dailyMenuRepository;
    @Mock GroupDailyMenuRepository groupDailyMenuRepository;
    @Mock DefaultGroupMenuRepository defaultGroupMenuRepository;
    @Mock StoreRepository storeRepository;
    @Mock WeeklyMenuNotificationStateService weeklyMenuNotificationStateService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock CurrentAccountProvider currentAccountProvider;

    @InjectMocks MenuGroupService menuGroupService;

    @Test
    @DisplayName("월~금 모두 메뉴가 있으면 true")
    void weeklyMenuFilled_true() {
        Long storeId = 1L;
        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(10L);
        when(menuGroupRepository.findByStoreIdOrderBySortOrderAscIdAsc(storeId)).thenReturn(List.of(group));

        LocalDate weekStart = LocalDate.of(2026, 2, 9); // Monday
        List<GroupDailyMenu> menus = List.of(
                filled(weekStart),
                filled(weekStart.plusDays(1)),
                filled(weekStart.plusDays(2)),
                filled(weekStart.plusDays(3)),
                filled(weekStart.plusDays(4))
        );
        when(groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(
                List.of(10L), weekStart, weekStart.plusDays(4)
        )).thenReturn(menus);

        assertTrue(menuGroupService.isWeeklyMenuFilled(storeId, weekStart.plusDays(2)));
    }

    @Test
    @DisplayName("하루라도 비어있으면 false")
    void weeklyMenuFilled_false() {
        Long storeId = 1L;
        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(10L);
        when(menuGroupRepository.findByStoreIdOrderBySortOrderAscIdAsc(storeId)).thenReturn(List.of(group));

        LocalDate weekStart = LocalDate.of(2026, 2, 9);
        List<GroupDailyMenu> menus = List.of(
                filled(weekStart),
                filled(weekStart.plusDays(1)),
                filled(weekStart.plusDays(3)),
                filled(weekStart.plusDays(4))
        );
        when(groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(
                List.of(10L), weekStart, weekStart.plusDays(4)
        )).thenReturn(menus);

        assertFalse(menuGroupService.isWeeklyMenuFilled(storeId, weekStart.plusDays(2)));
    }

    @Test
    @DisplayName("그룹별 주간 메뉴 완성 여부를 계산한다")
    void weeklyMenuFilledByGroup() {
        Long storeId = 1L;
        MenuGroup g1 = mock(MenuGroup.class);
        MenuGroup g2 = mock(MenuGroup.class);
        when(g1.getId()).thenReturn(10L);
        when(g2.getId()).thenReturn(20L);
        when(menuGroupRepository.findByStoreIdOrderBySortOrderAscIdAsc(storeId)).thenReturn(List.of(g1, g2));

        LocalDate weekStart = LocalDate.of(2026, 2, 9); // Monday
        List<GroupDailyMenu> menus = List.of(
                filledWithGroup(10L, weekStart),
                filledWithGroup(10L, weekStart.plusDays(1)),
                filledWithGroup(10L, weekStart.plusDays(2)),
                filledWithGroup(10L, weekStart.plusDays(3)),
                filledWithGroup(10L, weekStart.plusDays(4)),
                filledWithGroup(20L, weekStart),
                filledWithGroup(20L, weekStart.plusDays(1)),
                filledWithGroup(20L, weekStart.plusDays(3))
        );
        when(groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(
                List.of(10L, 20L), weekStart, weekStart.plusDays(4)
        )).thenReturn(menus);

        var result = menuGroupService.isWeeklyMenuFilledByGroup(storeId, weekStart.plusDays(2));

        assertTrue(result.get(10L));
        assertFalse(result.get(20L));
    }

    @Test
    @DisplayName("PENDING_LATE 상태에서 허용 기간 내 주간 메뉴가 완성되면 즉시 업로드 알림 대상이다")
    void shouldSendImmediateWeeklyUploadAlert_trueWhenPendingLateAndCompleted() {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate today = LocalDate.of(2026, 3, 18);
        LocalDate weekStart = LocalDate.of(2026, 3, 16);
        String weekKey = "2026-03-16";

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(groupId);
        when(menuGroupRepository.findByStoreIdOrderBySortOrderAscIdAsc(storeId)).thenReturn(List.of(group));
        when(dailyMenuRepository.findByStoreIdAndDateBetween(storeId, weekStart, weekStart.plusDays(4)))
                .thenReturn(List.of());
        List<GroupDailyMenu> completedMenus = List.of(
                filledWithGroup(groupId, weekStart),
                filledWithGroup(groupId, weekStart.plusDays(1)),
                filledWithGroup(groupId, weekStart.plusDays(2)),
                filledWithGroup(groupId, weekStart.plusDays(3)),
                filledWithGroup(groupId, weekStart.plusDays(4))
        );
        when(groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(
                List.of(groupId), weekStart, weekStart.plusDays(4)
        )).thenReturn(completedMenus);

        when(weeklyMenuNotificationStateService.findStatus(storeId, groupId, weekKey))
                .thenReturn(Optional.of(WeeklyMenuNotificationStatus.PENDING_LATE));

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(today);

            assertTrue(menuGroupService.shouldSendImmediateWeeklyUploadAlert(storeId, groupId, today));
        }
    }

    @Test
    @DisplayName("허용 기간이 지난 토요일에는 즉시 업로드 알림을 보내지 않고 CLOSED_NOT_SENT로 마감한다")
    void shouldSendImmediateWeeklyUploadAlert_falseAndCloseWhenWindowExpired() {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate today = LocalDate.of(2026, 3, 21);
        LocalDate dateInCurrentWeek = LocalDate.of(2026, 3, 18);
        String weekKey = "2026-03-16";

        when(weeklyMenuNotificationStateService.findStatus(storeId, groupId, weekKey))
                .thenReturn(Optional.of(WeeklyMenuNotificationStatus.PENDING_LATE));

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(today);

            assertFalse(menuGroupService.shouldSendImmediateWeeklyUploadAlert(storeId, groupId, dateInCurrentWeek));
        }

        verify(weeklyMenuNotificationStateService).markClosedNotSent(storeId, groupId, weekKey);
    }

    @Test
    @DisplayName("현재 알림 주차에 이미 SENT 상태면 메뉴 변경 알림 대상이다")
    void shouldSendMenuChangeAlert_trueWhenAlreadySentInCurrentWeek() {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate today = LocalDate.of(2026, 3, 18);
        LocalDate dateInCurrentWeek = LocalDate.of(2026, 3, 19);
        String weekKey = "2026-03-16";

        when(weeklyMenuNotificationStateService.findStatus(storeId, groupId, weekKey))
                .thenReturn(Optional.of(WeeklyMenuNotificationStatus.SENT));

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(today);

            assertTrue(menuGroupService.shouldSendMenuChangeAlert(storeId, groupId, dateInCurrentWeek));
        }
    }

    private GroupDailyMenu filled(LocalDate date) {
        GroupDailyMenu gdm = GroupDailyMenu.builder()
                .menuGroup(null)
                .date(date)
                .build();
        gdm.replaceMenus(List.of("menu"));
        return gdm;
    }

    private GroupDailyMenu filledWithGroup(Long groupId, LocalDate date) {
        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(groupId);
        GroupDailyMenu gdm = GroupDailyMenu.builder()
                .menuGroup(group)
                .date(date)
                .build();
        gdm.replaceMenus(List.of("menu"));
        return gdm;
    }
}
