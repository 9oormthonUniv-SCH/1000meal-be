package com._1000meal.menu.service;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.MenuPreset;
import com._1000meal.menu.dto.MenuPresetCreateRequest;
import com._1000meal.menu.dto.MenuPresetDetailResponse;
import com._1000meal.menu.dto.MenuPresetSummaryResponse;
import com._1000meal.menu.repository.MenuPresetRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    StoreRepository storeRepository;

    @Mock
    CurrentAccountProvider currentAccountProvider;

    @InjectMocks
    MenuPresetService menuPresetService;

    @Test
    @DisplayName("create: 성공 - 공백 제거 후 menus 저장")
    void create_success() throws Exception {
        Long storeId = 1L;
        Long accountId = 10L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(accountId);

        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        MenuPresetCreateRequest request = new MenuPresetCreateRequest();
        setField(request, "menus", List.of(" 김치찌개 ", " ", "김밥"));

        MenuPreset preset = MenuPreset.builder()
                .store(store)
                .createdByAccountId(accountId)
                .menus(List.of("김치찌개", "김밥"))
                .build();
        setField(preset, "id", 101L);
        setField(preset, "createdAt", LocalDateTime.of(2026, 2, 9, 10, 0));
        setField(preset, "updatedAt", LocalDateTime.of(2026, 2, 9, 10, 0));

        when(menuPresetRepository.save(any(MenuPreset.class))).thenReturn(preset);

        MenuPresetDetailResponse response = menuPresetService.create(storeId, request);

        assertEquals(101L, response.getId());
        assertEquals(List.of("김치찌개", "김밥"), response.getMenus());
        assertEquals("김치찌개, 김밥", response.getPreview());
    }

    @Test
    @DisplayName("create: 공백 제거 후 비어있으면 MENU_EMPTY")
    void create_emptyAfterTrim() {
        Long storeId = 1L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mock(Store.class)));

        MenuPresetCreateRequest request = new MenuPresetCreateRequest();
        setField(request, "menus", List.of("  ", ""));

        CustomException ex = assertThrows(CustomException.class,
                () -> menuPresetService.create(storeId, request));

        assertEquals(MenuErrorCode.MENU_EMPTY, ex.getErrorCodeIfs());
        verify(menuPresetRepository, never()).save(any());
    }

    @Test
    @DisplayName("list: 요약 응답 preview 생성")
    void list_success() throws Exception {
        Long storeId = 1L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mock(Store.class)));

        MenuPreset preset = MenuPreset.builder()
                .store(mock(Store.class))
                .createdByAccountId(10L)
                .menus(List.of("김치찌개", "김밥"))
                .build();
        setField(preset, "id", 1L);
        setField(preset, "createdAt", LocalDateTime.of(2026, 2, 9, 10, 0));
        setField(preset, "updatedAt", LocalDateTime.of(2026, 2, 9, 10, 0));

        when(menuPresetRepository.findByStoreIdWithMenus(storeId)).thenReturn(List.of(preset));

        List<MenuPresetSummaryResponse> result = menuPresetService.list(storeId);

        assertEquals(1, result.size());
        assertEquals("김치찌개, 김밥", result.get(0).getPreview());
    }

    @Test
    @DisplayName("get: 상세 조회 성공")
    void get_success() throws Exception {
        Long storeId = 1L;
        Long presetId = 2L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        MenuPreset preset = MenuPreset.builder()
                .store(store)
                .createdByAccountId(10L)
                .menus(List.of("우동", "라면"))
                .build();
        setField(preset, "id", presetId);
        setField(preset, "createdAt", LocalDateTime.of(2026, 2, 9, 10, 0));
        setField(preset, "updatedAt", LocalDateTime.of(2026, 2, 9, 10, 0));

        when(menuPresetRepository.findByIdAndStoreIdWithMenus(presetId, storeId))
                .thenReturn(Optional.of(preset));

        MenuPresetDetailResponse response = menuPresetService.get(storeId, presetId);

        assertEquals(presetId, response.getId());
        assertEquals(List.of("우동", "라면"), response.getMenus());
    }

    @Test
    @DisplayName("delete: 성공")
    void delete_success() {
        Long storeId = 1L;
        Long presetId = 3L;

        when(currentAccountProvider.getCurrentStoreId()).thenReturn(storeId);

        MenuPreset preset = MenuPreset.builder()
                .store(mock(Store.class))
                .createdByAccountId(10L)
                .menus(List.of("비빔밥"))
                .build();

        when(menuPresetRepository.findByIdAndStoreId(presetId, storeId))
                .thenReturn(Optional.of(preset));

        menuPresetService.delete(storeId, presetId);

        verify(menuPresetRepository).delete(preset);
    }

    @Test
    @DisplayName("store mismatch: STORE_ACCESS_DENIED")
    void storeMismatchDenied() {
        when(currentAccountProvider.getCurrentStoreId()).thenReturn(2L);

        CustomException ex = assertThrows(CustomException.class,
                () -> menuPresetService.list(1L));

        assertEquals(StoreErrorCode.STORE_ACCESS_DENIED, ex.getErrorCodeIfs());
        verifyNoInteractions(storeRepository, menuPresetRepository);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
