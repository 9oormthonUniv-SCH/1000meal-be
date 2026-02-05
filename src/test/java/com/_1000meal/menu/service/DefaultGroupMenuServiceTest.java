package com._1000meal.menu.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.repository.DefaultGroupMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.store.domain.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGroupMenuServiceTest {

    @Mock
    DefaultGroupMenuRepository defaultGroupMenuRepository;

    @Mock
    MenuGroupRepository menuGroupRepository;

    @InjectMocks
    DefaultGroupMenuService service;

    @Test
    @DisplayName("다른 매장 groupId로 핀 설정 시 STORE_ACCESS_DENIED")
    void pinDefaultMenu_forbiddenDifferentStore() {
        Long storeId = 1L;
        Long groupId = 10L;

        Store otherStore = org.mockito.Mockito.mock(Store.class);
        MenuGroup group = org.mockito.Mockito.mock(MenuGroup.class);
        when(group.getStore()).thenReturn(otherStore);
        when(otherStore.getId()).thenReturn(2L);

        when(menuGroupRepository.findByIdWithStore(groupId)).thenReturn(Optional.of(group));

        CustomException ex = assertThrows(CustomException.class,
                () -> service.pinDefaultMenu(storeId, groupId, "소보로빵"));

        assertEquals(StoreErrorCode.STORE_ACCESS_DENIED, ex.getErrorCodeIfs());
    }
}
