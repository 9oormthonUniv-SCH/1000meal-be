package com._1000meal.menu.service;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuPreset;
import com._1000meal.menu.dto.MenuPresetCreateRequest;
import com._1000meal.menu.dto.MenuPresetDetailResponse;
import com._1000meal.menu.dto.MenuPresetSummaryResponse;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuPresetRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuPresetServiceTest {

    @Mock
    MenuPresetRepository menuPresetRepository;

    @Mock
    MenuGroupRepository menuGroupRepository;

    @Mock
    StoreRepository storeRepository;

    @Mock
    CurrentAccountProvider currentAccountProvider;

    @InjectMocks
    MenuPresetService menuPresetService;

    @Test
    @DisplayName("create: success in store+group scope")
    void create_success() throws Exception {
        Long storeId = 1L;
        Long groupId = 10L;
        Long accountId = 100L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(accountId);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        MenuGroup group = mock(MenuGroup.class);
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdAndStoreId(groupId, storeId)).thenReturn(Optional.of(group));

        MenuPresetCreateRequest request = new MenuPresetCreateRequest();
        setField(request, "menus", List.of(" Kimchi ", " ", "Kimbap"));

        MenuPreset saved = MenuPreset.builder()
                .store(store)
                .groupId(groupId)
                .createdByAccountId(accountId)
                .menus(List.of("Kimchi", "Kimbap"))
                .build();
        setField(saved, "id", 101L);
        setField(saved, "createdAt", LocalDateTime.of(2026, 2, 12, 10, 0));
        setField(saved, "updatedAt", LocalDateTime.of(2026, 2, 12, 10, 0));

        when(menuPresetRepository.save(any(MenuPreset.class))).thenReturn(saved);

        MenuPresetDetailResponse response = menuPresetService.create(storeId, groupId, request);

        assertEquals(101L, response.getId());
        assertEquals(storeId, response.getStoreId());
        assertEquals(groupId, response.getGroupId());
        assertEquals(List.of("Kimchi", "Kimbap"), response.getMenus());
    }

    @Test
    @DisplayName("create: MENU_EMPTY when menus become empty after trim")
    void create_emptyAfterTrim() {
        Long storeId = 1L;
        Long groupId = 10L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mock(Store.class)));

        MenuGroup group = mock(MenuGroup.class);
        Store groupStore = mock(Store.class);
        when(groupStore.getId()).thenReturn(storeId);
        when(group.getStore()).thenReturn(groupStore);
        when(menuGroupRepository.findByIdAndStoreId(groupId, storeId)).thenReturn(Optional.of(group));

        MenuPresetCreateRequest request = new MenuPresetCreateRequest();
        setField(request, "menus", List.of(" ", ""));

        CustomException ex = assertThrows(CustomException.class,
                () -> menuPresetService.create(storeId, groupId, request));

        assertEquals(MenuErrorCode.MENU_EMPTY, ex.getErrorCodeIfs());
        verify(menuPresetRepository, never()).save(any());
    }

    @Test
    @DisplayName("list: MENU_PRESET_EMPTY when scoped list is empty")
    void list_empty() {
        Long storeId = 1L;
        Long groupId = 10L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mock(Store.class)));

        MenuGroup group = mock(MenuGroup.class);
        Store groupStore = mock(Store.class);
        when(groupStore.getId()).thenReturn(storeId);
        when(group.getStore()).thenReturn(groupStore);
        when(menuGroupRepository.findByIdAndStoreId(groupId, storeId)).thenReturn(Optional.of(group));

        when(menuPresetRepository.findAllByStoreIdAndGroupIdWithMenus(storeId, groupId)).thenReturn(List.of());

        CustomException ex = assertThrows(CustomException.class,
                () -> menuPresetService.list(storeId, groupId));

        assertEquals(MenuErrorCode.MENU_PRESET_EMPTY, ex.getErrorCodeIfs());
    }

    @Test
    @DisplayName("get: scoped detail success")
    void get_success() throws Exception {
        Long storeId = 1L;
        Long groupId = 10L;
        Long presetId = 101L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdAndStoreId(groupId, storeId)).thenReturn(Optional.of(group));

        MenuPreset preset = MenuPreset.builder()
                .store(store)
                .groupId(groupId)
                .createdByAccountId(100L)
                .menus(List.of("A", "B"))
                .build();
        setField(preset, "id", presetId);
        setField(preset, "createdAt", LocalDateTime.of(2026, 2, 12, 10, 0));
        setField(preset, "updatedAt", LocalDateTime.of(2026, 2, 12, 10, 0));

        when(menuPresetRepository.findByIdAndStoreIdAndGroupIdWithMenus(presetId, storeId, groupId))
                .thenReturn(Optional.of(preset));

        MenuPresetDetailResponse response = menuPresetService.get(storeId, groupId, presetId);

        assertEquals(presetId, response.getId());
        assertEquals(groupId, response.getGroupId());
    }

    @Test
    @DisplayName("delete: scoped delete success")
    void delete_success() {
        Long storeId = 1L;
        Long groupId = 10L;
        Long presetId = 101L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getStore()).thenReturn(store);
        when(menuGroupRepository.findByIdAndStoreId(groupId, storeId)).thenReturn(Optional.of(group));

        MenuPreset preset = MenuPreset.builder()
                .store(store)
                .groupId(groupId)
                .createdByAccountId(100L)
                .menus(List.of("A"))
                .build();

        when(menuPresetRepository.findByIdAndStoreIdAndGroupId(presetId, storeId, groupId))
                .thenReturn(Optional.of(preset));

        menuPresetService.delete(storeId, groupId, presetId);

        verify(menuPresetRepository).delete(preset);
    }

    @Test
    @DisplayName("group mismatch: MENU_GROUP_NOT_FOUND")
    void groupMismatch() {
        Long storeId = 1L;
        Long groupId = 99L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);
        when(menuGroupRepository.findByIdAndStoreId(groupId, storeId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> menuPresetService.list(storeId, groupId));

        assertEquals(MenuErrorCode.MENU_GROUP_NOT_FOUND, ex.getErrorCodeIfs());
    }

    @Test
    @DisplayName("store mismatch: STORE_ACCESS_DENIED")
    void storeMismatchDenied() {
        when(currentAccountProvider.getCurrentStoreId()).thenReturn(2L);

        CustomException ex = assertThrows(CustomException.class,
                () -> menuPresetService.list(1L, 10L));

        assertEquals(StoreErrorCode.STORE_ACCESS_DENIED, ex.getErrorCodeIfs());
        verifyNoInteractions(storeRepository, menuGroupRepository, menuPresetRepository);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

