package com._1000meal.store.service;

import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.store.dto.StoreResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreViewServiceTest {

    @Mock private StoreService storeService;
    @Mock private DailyMenuRepository dailyMenuRepository;

    @InjectMocks private StoreViewService storeViewService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("getAllStoresView: liveStock 있으면 todayMenu.stock을 liveStock으로 덮어쓴다")
    void getAllStoresView_overwriteStock_whenLiveStockPresent() {
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        DailyMenuDto baseMenu = DailyMenuDto.builder()
                .stock(5)
                .build();

        StoreResponse sr1 = StoreResponse.builder()
                .id(1L)
                .todayMenu(baseMenu)
                .build();

        when(storeService.getAllStores()).thenReturn(List.of(sr1));
        when(dailyMenuRepository.findTotalGroupStockByStoreIdAndDate(1L, fixedToday))
                .thenReturn(Optional.of(12));

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            List<StoreResponse> result = storeViewService.getAllStoresView();

            assertEquals(1, result.size());
            assertNotNull(result.get(0).getTodayMenu());
            assertEquals(12, result.get(0).getTodayMenu().getStock());
        }

        verify(storeService).getAllStores();
        verify(dailyMenuRepository).findTotalGroupStockByStoreIdAndDate(1L, fixedToday);
    }

    @Test
    @DisplayName("getAllStoresView: liveStock 없으면 base(todayMenu.stock)를 유지한다")
    void getAllStoresView_keepBaseStock_whenLiveStockAbsent() {
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        DailyMenuDto baseMenu = DailyMenuDto.builder()
                .stock(7)
                .build();

        StoreResponse sr1 = StoreResponse.builder()
                .id(1L)
                .todayMenu(baseMenu)
                .build();

        when(storeService.getAllStores()).thenReturn(List.of(sr1));
        when(dailyMenuRepository.findTotalGroupStockByStoreIdAndDate(1L, fixedToday))
                .thenReturn(Optional.empty());

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            List<StoreResponse> result = storeViewService.getAllStoresView();

            assertEquals(1, result.size());
            assertNotNull(result.get(0).getTodayMenu());
            assertEquals(7, result.get(0).getTodayMenu().getStock()); // ✅ 유지
        }

        verify(storeService).getAllStores();
        verify(dailyMenuRepository).findTotalGroupStockByStoreIdAndDate(1L, fixedToday);
    }

    @Test
    @DisplayName("getAllStoresView: todayMenu가 null이면 null 유지 + liveStock 조회 fallback도 null")
    void getAllStoresView_todayMenuNull_staysNull() {
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        StoreResponse sr1 = StoreResponse.builder()
                .id(1L)
                .todayMenu(null)
                .build();

        when(storeService.getAllStores()).thenReturn(List.of(sr1));
        when(dailyMenuRepository.findTotalGroupStockByStoreIdAndDate(1L, fixedToday))
                .thenReturn(Optional.empty());

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            List<StoreResponse> result = storeViewService.getAllStoresView();

            assertEquals(1, result.size());
            assertNull(result.get(0).getTodayMenu());
        }

        verify(storeService).getAllStores();
        verify(dailyMenuRepository).findTotalGroupStockByStoreIdAndDate(1L, fixedToday);
    }
}