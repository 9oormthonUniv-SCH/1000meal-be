package com._1000meal.store.service;

import com._1000meal.menu.service.MenuGroupService;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.dto.StoreTodayMenuDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreViewServiceTest {

    @Mock private StoreService storeService;
    @Mock private MenuGroupService menuGroupService;

    @InjectMocks private StoreViewService storeViewService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("getAllStoresView: todayMenu를 일간 메뉴 조회 결과로 교체한다")
    void getAllStoresView_replacesTodayMenuFromService() {
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        StoreTodayMenuDto baseMenu = StoreTodayMenuDto.builder().build();
        StoreResponse sr1 = StoreResponse.builder().id(1L).todayMenu(baseMenu).build();

        when(storeService.getAllStores()).thenReturn(List.of(sr1));
        StoreTodayMenuDto todayMenu = StoreTodayMenuDto.builder()
                .isOpen(true)
                .isHoliday(false)
                .build();
        when(menuGroupService.getTodayMenuForStores(List.of(1L), fixedToday))
                .thenReturn(Map.of(1L, todayMenu));

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            List<StoreResponse> result = storeViewService.getAllStoresView();

            assertEquals(1, result.size());
            assertSame(todayMenu, result.get(0).getTodayMenu());
            assertTrue(result.get(0).isOpen());
            assertFalse(result.get(0).isHoliday());
        }

        verify(storeService).getAllStores();
        verify(menuGroupService).getTodayMenuForStores(List.of(1L), fixedToday);
    }

    @Test
    @DisplayName("getAllStoresView: todayMenu 없으면 기존 값을 유지한다")
    void getAllStoresView_keepBase_whenNoTodayMenu() {
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        StoreTodayMenuDto baseMenu = StoreTodayMenuDto.builder().build();

        StoreResponse sr1 = StoreResponse.builder()
                .id(1L)
                .todayMenu(baseMenu)
                .build();

        when(storeService.getAllStores()).thenReturn(List.of(sr1));
        when(menuGroupService.getTodayMenuForStores(List.of(1L), fixedToday))
                .thenReturn(Map.of());

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            List<StoreResponse> result = storeViewService.getAllStoresView();

            assertEquals(1, result.size());
            assertSame(baseMenu, result.get(0).getTodayMenu());
        }

        verify(storeService).getAllStores();
        verify(menuGroupService).getTodayMenuForStores(List.of(1L), fixedToday);
    }

    @Test
    @DisplayName("getAllStoresView: todayMenu가 null이고 신규 값이 없으면 null 유지")
    void getAllStoresView_todayMenuNull_staysNull() {
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        StoreResponse sr1 = StoreResponse.builder()
                .id(1L)
                .todayMenu(null)
                .build();

        when(storeService.getAllStores()).thenReturn(List.of(sr1));
        when(menuGroupService.getTodayMenuForStores(List.of(1L), fixedToday))
                .thenReturn(Map.of());

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            List<StoreResponse> result = storeViewService.getAllStoresView();

            assertEquals(1, result.size());
            assertNull(result.get(0).getTodayMenu());
        }

        verify(storeService).getAllStores();
        verify(menuGroupService).getTodayMenuForStores(List.of(1L), fixedToday);
    }
}
