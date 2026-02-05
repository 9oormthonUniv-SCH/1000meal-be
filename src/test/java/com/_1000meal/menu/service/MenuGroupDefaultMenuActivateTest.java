package com._1000meal.menu.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DefaultGroupMenu;
import com._1000meal.menu.domain.GroupDailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.dto.DefaultMenuActivateRequest;
import com._1000meal.menu.dto.DefaultMenuActivateResponse;
import com._1000meal.menu.repository.DefaultGroupMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.auth.service.CurrentAccountProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuGroupDefaultMenuActivateTest {

    @Mock DefaultGroupMenuRepository defaultGroupMenuRepository;
    @Mock MenuGroupRepository menuGroupRepository;
    @Mock GroupDailyMenuRepository groupDailyMenuRepository;
    @Mock CurrentAccountProvider currentAccountProvider;

    @InjectMocks DefaultGroupMenuService service;

    @Test
    @DisplayName("다른 매장 groupId로 activate 호출 시 STORE_403")
    void activateDefaultMenu_forbiddenDifferentStore() {
        Long storeId = 1L;
        Long groupId = 10L;
        Long defaultMenuId = 99L;

        Store otherStore = mock(Store.class);
        when(otherStore.getId()).thenReturn(2L);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getStore()).thenReturn(otherStore);
        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));

        CustomException ex = assertThrows(CustomException.class,
                () -> service.activateDefaultMenu(storeId, groupId, defaultMenuId, null));

        assertEquals(StoreErrorCode.STORE_ACCESS_DENIED, ex.getErrorCodeIfs());
        verifyNoInteractions(defaultGroupMenuRepository);
    }

    @Test
    @DisplayName("같은 매장 activate 시 default 활성화 + daily 생성")
    void activateDefaultMenu_successCreatesDaily() {
        Long storeId = 1L;
        Long groupId = 10L;
        Long defaultMenuId = 99L;
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
                .active(false)
                .startDate(today.minusDays(1))
                .endDate(null)
                .build();

        when(defaultGroupMenuRepository.findById(defaultMenuId)).thenReturn(Optional.of(rule));
        when(defaultGroupMenuRepository.findActiveByMenuGroupIdAndDate(groupId, today))
                .thenReturn(List.of(rule));

        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, today))
                .thenReturn(Optional.empty());
        when(groupDailyMenuRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(18L);

        DefaultMenuActivateRequest request = new DefaultMenuActivateRequest();
        setField(request, "startDate", today);

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(today);

            DefaultMenuActivateResponse response = service.activateDefaultMenu(
                    storeId, groupId, defaultMenuId, request
            );

            assertNotNull(response);
            assertTrue(response.getDefaultMenu().isActive());
            assertEquals(today, response.getDefaultMenu().getStartDate());
            assertEquals(1, response.getMaterialized().getItemCount());
            assertFalse(response.getMaterialized().isReplaced());
            assertEquals(List.of("국수"), response.getMaterialized().getMenus());
        }
    }

    @Test
    @DisplayName("기존 daily가 있는 경우 items 교체")
    void activateDefaultMenu_replacesDaily() throws Exception {
        Long storeId = 1L;
        Long groupId = 10L;
        Long defaultMenuId = 99L;
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
                .menuName("김밥")
                .active(true)
                .startDate(today.minusDays(1))
                .endDate(null)
                .build();

        when(defaultGroupMenuRepository.findById(defaultMenuId)).thenReturn(Optional.of(rule));
        when(defaultGroupMenuRepository.findActiveByMenuGroupIdAndDate(groupId, today))
                .thenReturn(List.of(rule));

        GroupDailyMenu existing = GroupDailyMenu.builder()
                .menuGroup(group)
                .date(today)
                .build();
        setEntityId(existing, 100L);
        existing.replaceMenus(List.of("기존메뉴"));

        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, today))
                .thenReturn(Optional.of(existing));
        when(groupDailyMenuRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(18L);

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(ZoneId.of("Asia/Seoul"))).thenReturn(today);

            DefaultMenuActivateResponse response = service.activateDefaultMenu(
                    storeId, groupId, defaultMenuId, null
            );

            assertTrue(response.getMaterialized().isReplaced());
        }

        ArgumentCaptor<GroupDailyMenu> captor = ArgumentCaptor.forClass(GroupDailyMenu.class);
        verify(groupDailyMenuRepository).save(captor.capture());
        assertEquals(List.of("김밥"), captor.getValue().getMenuNames());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setEntityId(Object target, Long id) {
        setField(target, "id", id);
    }
}
