package com._1000meal.store.service;


import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.dto.WeeklyMenuWithGroupsResponse;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.service.MenuGroupService;
import com._1000meal.menu.service.MenuService;
import com._1000meal.store.domain.Store;
import com._1000meal.store.dto.StoreDetailedResponse;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.dto.StoreTodayMenuDto;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock private StoreRepository storeRepository;
    @Mock private DailyMenuRepository dailyMenuRepository;
    @Mock private MenuService menuService;
    @Mock private MenuGroupService menuGroupService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private StoreService storeService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("getStoreDetail: store 없으면 STORE_NOT_FOUND")
    void getStoreDetail_storeNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> storeService.getStoreDetail(1L));

        assertEquals(StoreErrorCode.STORE_NOT_FOUND, ex.getErrorCodeIfs());
        assertEquals(StoreErrorCode.STORE_NOT_FOUND.getMessage(), ex.getDisplayMessage());
    }

    @Test
    @DisplayName("getStoreDetail: 오늘 날짜 기준 weeklyMenu 조회 + store remain/isOpen 반영")
    void getStoreDetail_success_reflectsStoreFields() {
        Long storeId = 1L;
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        Store store = Store.builder()
                .name("store")
                .address("addr")
                .phone("010-0000-0000")
                .description("desc")
                .isOpen(true)
                .remain(12)
                .build();
        WeeklyMenuWithGroupsResponse weekly = mock(WeeklyMenuWithGroupsResponse.class);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuService.getWeeklyMenuWithGroups(eq(storeId), eq(fixedToday))).thenReturn(weekly);

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            StoreDetailedResponse res = storeService.getStoreDetail(storeId);

            assertTrue(res.isOpen());
            assertEquals(12, res.getRemain());
        }

        verify(storeRepository).findById(storeId);
        verify(menuService).getWeeklyMenuWithGroups(storeId, fixedToday);
        verifyNoInteractions(dailyMenuRepository);
    }

    @Test
    @DisplayName("getStoreDetail: dailyMenu 없이도 store remain/isOpen 유지")
    void getStoreDetail_noDailyMenu_stillUsesStoreFields() {
        Long storeId = 1L;
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        Store store = Store.builder()
                .name("store")
                .address("addr")
                .phone("010-0000-0000")
                .description("desc")
                .isOpen(true)
                .remain(50)
                .build();
        WeeklyMenuWithGroupsResponse weekly = mock(WeeklyMenuWithGroupsResponse.class);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuService.getWeeklyMenuWithGroups(eq(storeId), eq(fixedToday))).thenReturn(weekly);

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            StoreDetailedResponse res = storeService.getStoreDetail(storeId);

            assertTrue(res.isOpen());
            assertEquals(50, res.getRemain());
        }

        verifyNoInteractions(dailyMenuRepository);
    }

    @Test
    @DisplayName("getAllStores: 각 storeId에 대해 store 조회 + 오늘 메뉴/holiday 계산")
    void getAllStores_success_holidayLogic() {
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        // 2개 매장
        when(storeRepository.findAllStoreIds()).thenReturn(List.of(1L, 2L));

        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);

        when(storeRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(store1, store2));
        when(store1.getId()).thenReturn(1L);
        when(store2.getId()).thenReturn(2L);

        StoreTodayMenuDto dm1Dto = StoreTodayMenuDto.builder()
                .date(fixedToday)
                .dayOfWeek(fixedToday.getDayOfWeek())
                .isOpen(true)
                .isHoliday(false)
                .build();
        StoreTodayMenuDto dm2Dto = StoreTodayMenuDto.builder()
                .date(fixedToday)
                .dayOfWeek(fixedToday.getDayOfWeek())
                .isOpen(true)
                .isHoliday(false)
                .build();

        when(menuGroupService.getTodayMenuForStores(List.of(1L, 2L), fixedToday))
                .thenReturn(Map.of(1L, dm1Dto, 2L, dm2Dto));

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            List<StoreResponse> result = storeService.getAllStores();

            assertEquals(2, result.size());
            assertSame(dm1Dto, result.get(0).getTodayMenu());
            assertSame(dm2Dto, result.get(1).getTodayMenu());
            assertTrue(result.get(0).isOpen());
            assertFalse(result.get(0).isHoliday());
        }

        verify(storeRepository).findAllStoreIds();
        verify(storeRepository).findAllById(List.of(1L, 2L));
        verify(menuGroupService).getTodayMenuForStores(List.of(1L, 2L), fixedToday);
    }

    @Test
    @DisplayName("toggleStoreStatus: store isOpen 토글 + 오늘 dailyMenu 있으면 같이 토글")
    void toggleStoreStatus_togglesStoreAndDailyMenu() {
        Long storeId = 1L;
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        DailyMenu dm = mock(DailyMenu.class);
        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, fixedToday)).thenReturn(Optional.of(dm));

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            String msg = storeService.toggleStoreStatus(storeId);
            assertEquals("가게와 오늘의 메뉴 운영 상태가 업데이트 되었습니다.", msg);
        }

        verify(store).toggleIsOpen();
        verify(dm).toggleIsOpen();
    }

    @Test
    @DisplayName("toggleStoreStatus: 오늘 dailyMenu 없으면 store만 토글")
    void toggleStoreStatus_noDailyMenu_onlyStoreToggles() {
        Long storeId = 1L;
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, fixedToday)).thenReturn(Optional.empty());

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            storeService.toggleStoreStatus(storeId);
        }

        verify(store).toggleIsOpen();
        // dailyMenu 없으므로 토글 호출 없어야 함
        verifyNoMoreInteractions(menuService);
    }

    @Test
    @DisplayName("toggleStoreStatus 이후 getStoreDetail: 메뉴와 무관하게 store.isOpen 반영")
    void toggleStoreStatus_thenGetStoreDetail_reflectsStoreOpen() {
        Long storeId = 1L;
        LocalDate fixedToday = LocalDate.of(2026, 1, 7);

        Store store = Store.builder()
                .name("store")
                .address("addr")
                .phone("010-0000-0000")
                .description("desc")
                .isOpen(false)
                .remain(10)
                .build();
        WeeklyMenuWithGroupsResponse weekly = mock(WeeklyMenuWithGroupsResponse.class);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuService.getWeeklyMenuWithGroups(eq(storeId), eq(fixedToday))).thenReturn(weekly);
        when(dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, fixedToday)).thenReturn(Optional.empty());

        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(() -> LocalDate.now(KST)).thenReturn(fixedToday);

            storeService.toggleStoreStatus(storeId);
            StoreDetailedResponse res = storeService.getStoreDetail(storeId);

            assertTrue(res.isOpen());
        }
    }

    @Test
    @DisplayName("setImageUrl: store 없으면 STORE_NOT_FOUND")
    void setImageUrl_storeNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> storeService.setImageUrl(1L, "http://img"));

        assertEquals(StoreErrorCode.STORE_NOT_FOUND, ex.getErrorCodeIfs());
        assertEquals(StoreErrorCode.STORE_NOT_FOUND.getMessage(), ex.getDisplayMessage());
    }

    @Test
    @DisplayName("setImageUrl: updateImageUrl 호출 + 변경된 url 반환")
    void setImageUrl_success() {
        Store store = mock(Store.class);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        when(store.getImageUrl()).thenReturn("http://changed");

        String res = storeService.setImageUrl(1L, "http://new");

        assertEquals("http://changed", res);
        verify(store).updateImageUrl("http://new");
        verify(store).getImageUrl();
    }
}
