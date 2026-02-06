package com._1000meal.menu.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.dto.GroupDailyMenuResponse;
import com._1000meal.menu.dto.MenuUpdateRequest;
import com._1000meal.menu.repository.DefaultGroupMenuRepository;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import com._1000meal.auth.service.CurrentAccountProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuGroupMenuReplaceServiceTest {

    @Mock MenuGroupRepository menuGroupRepository;
    @Mock MenuGroupStockRepository stockRepository;
    @Mock DailyMenuRepository dailyMenuRepository;
    @Mock GroupDailyMenuRepository groupDailyMenuRepository;
    @Mock DefaultGroupMenuRepository defaultGroupMenuRepository;
    @Mock StoreRepository storeRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock CurrentAccountProvider currentAccountProvider;

    @InjectMocks MenuGroupService service;

    @Test
    @DisplayName("다른 매장 groupId로 메뉴 등록/교체 시 STORE_403")
    void updateMenusInGroup_forbiddenDifferentStore() {
        Long groupId = 10L;
        Long accountStoreId = 1L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(accountStoreId);

        Store otherStore = mock(Store.class);
        when(otherStore.getId()).thenReturn(2L);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getStore()).thenReturn(otherStore);
        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));

        MenuUpdateRequest request = new MenuUpdateRequest(java.util.List.of("김밥"));

        CustomException ex = assertThrows(CustomException.class,
                () -> service.updateMenusInGroup(groupId, LocalDate.of(2026, 2, 5), request));

        assertEquals(StoreErrorCode.STORE_ACCESS_DENIED, ex.getErrorCodeIfs());
        verifyNoInteractions(groupDailyMenuRepository);
    }

    @Test
    @DisplayName("같은 매장 groupId로 메뉴 등록/교체 성공")
    void updateMenusInGroup_successSameStore() {
        Long groupId = 10L;
        Long accountStoreId = 1L;
        LocalDate date = LocalDate.of(2026, 2, 5);

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(accountStoreId);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(accountStoreId);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(groupId);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));

        when(groupDailyMenuRepository.findByMenuGroupIdAndDate(groupId, date))
                .thenReturn(Optional.empty());
        when(groupDailyMenuRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MenuUpdateRequest request = new MenuUpdateRequest(java.util.List.of("김밥", "국수"));

        GroupDailyMenuResponse response = service.updateMenusInGroup(groupId, date, request);

        assertNotNull(response);
        assertEquals(groupId, response.getGroupId());
        verify(groupDailyMenuRepository).save(any());
    }
}
