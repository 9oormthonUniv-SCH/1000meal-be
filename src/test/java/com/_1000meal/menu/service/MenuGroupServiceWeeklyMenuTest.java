package com._1000meal.menu.service;

import com._1000meal.auth.service.CurrentAccountProvider;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceWeeklyMenuTest {

    @Mock MenuGroupRepository menuGroupRepository;
    @Mock MenuGroupStockRepository stockRepository;
    @Mock DailyMenuRepository dailyMenuRepository;
    @Mock GroupDailyMenuRepository groupDailyMenuRepository;
    @Mock DefaultGroupMenuRepository defaultGroupMenuRepository;
    @Mock StoreRepository storeRepository;
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
