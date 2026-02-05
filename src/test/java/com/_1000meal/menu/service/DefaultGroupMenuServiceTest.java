package com._1000meal.menu.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DefaultGroupMenu;
import com._1000meal.menu.domain.GroupDailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.dto.DefaultMenuResponse;
import com._1000meal.menu.repository.DefaultGroupMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.auth.service.CurrentAccountProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DefaultGroupMenuServiceTest {

    @Mock
    DefaultGroupMenuRepository defaultGroupMenuRepository;

    @Mock
    MenuGroupRepository menuGroupRepository;

    @Mock
    GroupDailyMenuRepository groupDailyMenuRepository;

    @Mock
    CurrentAccountProvider currentAccountProvider;

    @InjectMocks
    DefaultGroupMenuService service;

    @Test
    @DisplayName("다른 매장 groupId로 핀 설정 시 STORE_ACCESS_DENIED")
    void pinDefaultMenu_forbiddenDifferentStore() {
        Long storeId = 1L;
        Long groupId = 10L;

        Store otherStore = mock(Store.class);
        MenuGroup group = mock(MenuGroup.class);
        when(group.getStore()).thenReturn(otherStore);
        when(otherStore.getId()).thenReturn(2L);

        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));

        CustomException ex = assertThrows(CustomException.class,
                () -> service.pinDefaultMenu(storeId, groupId, "소보로빵", null, null));

        assertEquals(StoreErrorCode.STORE_ACCESS_DENIED, ex.getErrorCodeIfs());
        verifyNoInteractions(defaultGroupMenuRepository);
    }

    @Test
    @DisplayName("POST startDate=today → active=true, 오늘 daily 없으면 생성")
    void pinDefaultMenu_today_materializeIfAbsent() {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate today = LocalDate.of(2026, 2, 5);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(groupId);
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));
        when(groupDailyMenuRepository.findDistinctMenuNamesByGroupId(groupId))
                .thenReturn(List.of("국수"));

        when(defaultGroupMenuRepository.findOpenRule(groupId, "국수")).thenReturn(Optional.empty());

        DefaultGroupMenu rule = DefaultGroupMenu.builder()
                .store(store)
                .menuGroup(group)
                .menuName("국수")
                .active(true)
                .startDate(today)
                .endDate(null)
                .build();
        when(defaultGroupMenuRepository.save(any())).thenReturn(rule);
        when(defaultGroupMenuRepository.findActiveByMenuGroupIdAndDate(groupId, today))
                .thenReturn(List.of(rule));
        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, today))
                .thenReturn(Optional.empty());

        doAnswer(inv -> inv.getArgument(0)).when(groupDailyMenuRepository).save(any());
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(18L);

        try (var mocked = org.mockito.Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(today);

            DefaultMenuResponse response = service.pinDefaultMenu(storeId, groupId, "국수", today, null);

            assertNotNull(response);
            assertTrue(response.isActive());
            verify(groupDailyMenuRepository).save(any());
        }
    }

    @Test
    @DisplayName("POST startDate=tomorrow → active=true, 오늘은 materialize 안 함")
    void pinDefaultMenu_tomorrow_noMaterializeToday() {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate today = LocalDate.of(2026, 2, 5);
        LocalDate tomorrow = today.plusDays(1);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(groupId);
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));
        when(groupDailyMenuRepository.findDistinctMenuNamesByGroupId(groupId))
                .thenReturn(List.of("국수"));

        when(defaultGroupMenuRepository.findOpenRule(groupId, "국수")).thenReturn(Optional.empty());

        DefaultGroupMenu rule = DefaultGroupMenu.builder()
                .store(store)
                .menuGroup(group)
                .menuName("국수")
                .active(true)
                .startDate(tomorrow)
                .endDate(null)
                .build();
        when(defaultGroupMenuRepository.save(any())).thenReturn(rule);

        try (var mocked = org.mockito.Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(today);

            DefaultMenuResponse response = service.pinDefaultMenu(storeId, groupId, "국수", tomorrow, null);

            assertTrue(response.isActive());
            verify(groupDailyMenuRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("그룹에 없는 메뉴로 고정메뉴 등록 시 400")
    void pinDefaultMenu_invalidMenuName() {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate today = LocalDate.of(2026, 2, 5);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(groupId);
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));

        when(groupDailyMenuRepository.findDistinctMenuNamesByGroupId(groupId))
                .thenReturn(List.of("김밥"));

        try (var mocked = org.mockito.Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(today);

            CustomException ex = assertThrows(CustomException.class,
                    () -> service.pinDefaultMenu(storeId, groupId, "생강", today, null));

            assertEquals(MenuErrorCode.DEFAULT_MENU_INVALID_MENU, ex.getErrorCodeIfs());
        }
    }

    @Test
    @DisplayName("고정메뉴 해제 시 오늘 daily에서 즉시 제거")
    void unpinDefaultMenu_removesTodayDaily() throws Exception {
        Long storeId = 1L;
        Long groupId = 10L;
        LocalDate today = LocalDate.of(2026, 2, 5);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(groupId);
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));

        DefaultGroupMenu rule = DefaultGroupMenu.builder()
                .store(store)
                .menuGroup(group)
                .menuName("국수")
                .active(true)
                .startDate(today.minusDays(1))
                .endDate(null)
                .build();
        when(defaultGroupMenuRepository.findOpenRule(groupId, "국수")).thenReturn(Optional.of(rule));
        when(defaultGroupMenuRepository.findActiveByMenuGroupIdAndDate(groupId, today))
                .thenReturn(List.of());

        GroupDailyMenu existing = GroupDailyMenu.builder()
                .menuGroup(group)
                .date(today)
                .build();
        setEntityId(existing, 100L);
        existing.replaceMenus(List.of("국수", "김밥"));

        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, today))
                .thenReturn(Optional.of(existing));
        when(groupDailyMenuRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(18L);

        try (var mocked = org.mockito.Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(today);

            DefaultMenuResponse response = service.unpinDefaultMenu(storeId, groupId, "국수");
            assertNotNull(response);
            assertTrue(response.getEndDate().isEqual(today));
        }

        verify(groupDailyMenuRepository).save(any());
        assertTrue(existing.getMenuNames().isEmpty());
    }

    private static void setEntityId(Object target, Long id) throws Exception {
        var field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
