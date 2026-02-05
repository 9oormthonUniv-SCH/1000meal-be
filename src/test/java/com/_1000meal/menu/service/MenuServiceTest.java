package com._1000meal.menu.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuGroupResponse;
import com._1000meal.menu.dto.DailyMenuWithGroupsDto;
import com._1000meal.menu.dto.MenuGroupDto;
import com._1000meal.menu.dto.MenuItemDto;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.dto.WeeklyMenuWithGroupsResponse;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuRepository;
import com._1000meal.menu.repository.WeeklyMenuRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock WeeklyMenuRepository weeklyMenuRepository;
    @Mock DailyMenuRepository dailyMenuRepository;
    @Mock GroupDailyMenuRepository groupDailyMenuRepository;
    @Mock MenuGroupRepository menuGroupRepository;
    @Mock MenuRepository menuRepository;
    @Mock MenuGroupService menuGroupService;

    @InjectMocks MenuService service;

    private LocalDate wed;
    private LocalDate weekStart;
    private LocalDate weekEnd;

    @BeforeEach
    void setUp() {
        wed = LocalDate.of(2026, 1, 7); // 수요일
        weekStart = wed.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        weekEnd = weekStart.plusDays(6);
    }

    // ========================================
    // getWeeklyMenu Tests
    // ========================================

    @Test
    @DisplayName("getWeeklyMenu: 매장 없으면 STORE_NOT_FOUND")
    void getWeeklyMenu_storeNotFound() {
        Long storeId = 1L;

        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.getWeeklyMenu(storeId, wed));

        assertEquals(StoreErrorCode.STORE_NOT_FOUND, ex.getErrorCodeIfs());
        assertEquals(StoreErrorCode.STORE_NOT_FOUND.getMessage(), ex.getDisplayMessage());
    }

    @Test
    @DisplayName("getWeeklyMenu: 주간 메뉴가 없으면 스켈레톤(월~금, 5일) 반환")
    void getWeeklyMenu_whenWeeklyMissing_returnsSkeleton() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(weeklyMenuRepository.findByStoreIdAndRangeWithDailyMenus(storeId, wed)).thenReturn(Optional.empty());

        WeeklyMenuResponse res = service.getWeeklyMenu(storeId, wed);

        assertEquals(storeId, res.getStoreId());
        assertEquals(weekStart, res.getStartDate());
        assertEquals(weekEnd, res.getEndDate());
        assertNotNull(res.getDailyMenus());
        assertEquals(5, res.getDailyMenus().size());

        assertEquals(weekStart, res.getDailyMenus().get(0).getDate());
        assertEquals(DayOfWeek.MONDAY, res.getDailyMenus().get(0).getDayOfWeek());
    }

    // ========================================
    // getWeeklyMenuWithGroups Tests
    // ========================================

    @Test
    @DisplayName("getWeeklyMenuWithGroups: 매장 없으면 STORE_NOT_FOUND")
    void getWeeklyMenuWithGroups_storeNotFound() {
        Long storeId = 1L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.getWeeklyMenuWithGroups(storeId, wed));

        assertEquals(StoreErrorCode.STORE_NOT_FOUND, ex.getErrorCodeIfs());
    }

    @Test
    @DisplayName("getWeeklyMenuWithGroups: 주간 메뉴가 없으면 스켈레톤(월~금, isOpen=true) 반환")
    void getWeeklyMenuWithGroups_whenWeeklyMissing_returnsSkeleton() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuGroupService.getMenuGroups(eq(storeId), any(LocalDate.class)))
                .thenAnswer(inv -> {
                    LocalDate d = inv.getArgument(1);
                    return DailyMenuWithGroupsDto.builder()
                            .id(null)
                            .date(d)
                            .dayOfWeek(d.getDayOfWeek())
                            .isOpen(true)
                            .isHoliday(false)
                            .totalStock(0)
                            .groups(List.of())
                            .build();
                });

        WeeklyMenuWithGroupsResponse res = service.getWeeklyMenuWithGroups(storeId, wed);

        assertEquals(storeId, res.getStoreId());
        assertEquals(weekStart, res.getStartDate());
        assertEquals(weekEnd, res.getEndDate());
        assertNotNull(res.getDailyMenus());
        assertEquals(5, res.getDailyMenus().size());

        DailyMenuGroupResponse firstDay = res.getDailyMenus().get(0);
        assertNull(firstDay.getId());
        assertEquals(weekStart, firstDay.getDate());
        assertEquals(DayOfWeek.MONDAY, firstDay.getDayOfWeek());
        assertTrue(firstDay.isOpen());
        assertFalse(firstDay.isHoliday());
        assertEquals(0, firstDay.getGroups().size());
    }

    @Test
    @DisplayName("getWeeklyMenuWithGroups: 주간 메뉴 존재하는 경우 그룹과 함께 반환")
    void getWeeklyMenuWithGroups_whenExists_returnsWithGroups() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        MenuGroupDto groupDto = MenuGroupDto.builder()
                .id(1L)
                .name("향설 1관")
                .sortOrder(0)
                .stock(90)
                .capacity(100)
                .menus(List.of("떡볶이"))
                .menuItems(List.of(new MenuItemDto("떡볶이", true)))
                .build();

        when(menuGroupService.getMenuGroups(eq(storeId), any(LocalDate.class)))
                .thenAnswer(inv -> {
                    LocalDate d = inv.getArgument(1);
                    return DailyMenuWithGroupsDto.builder()
                            .id(null)
                            .date(d)
                            .dayOfWeek(d.getDayOfWeek())
                            .isOpen(true)
                            .isHoliday(false)
                            .totalStock(90)
                            .groups(List.of(groupDto))
                            .build();
                });

        WeeklyMenuWithGroupsResponse res = service.getWeeklyMenuWithGroups(storeId, wed);

        assertEquals(5, res.getDailyMenus().size());

        DailyMenuGroupResponse wedResponse = res.getDailyMenus().stream()
                .filter(d -> d.getDate().equals(wed))
                .findFirst()
                .orElseThrow();

        assertEquals(10L, wedResponse.getId());
        assertTrue(wedResponse.isOpen());
        assertEquals(1, wedResponse.getGroups().size());
        assertEquals("향설 1관", wedResponse.getGroups().get(0).getName());
        assertEquals(90, wedResponse.getGroups().get(0).getStock());
        assertEquals(100, wedResponse.getGroups().get(0).getCapacity());
        assertEquals(List.of("떡볶이"), wedResponse.getGroups().get(0).getMenus());
        assertTrue(wedResponse.getGroups().get(0).getMenuItems().get(0).isPinned());
    }

    @Test
    @DisplayName("getWeeklyMenuWithGroups: 주간은 일간 조회 로직을 재사용")
    void getWeeklyMenuWithGroups_reusesDailyLogic() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        when(menuGroupService.getMenuGroups(eq(storeId), any(LocalDate.class)))
                .thenAnswer(inv -> {
                    LocalDate d = inv.getArgument(1);
                    return DailyMenuWithGroupsDto.builder()
                            .id(null)
                            .date(d)
                            .dayOfWeek(d.getDayOfWeek())
                            .isOpen(true)
                            .isHoliday(false)
                            .totalStock(0)
                            .groups(List.of())
                            .build();
                });

        WeeklyMenuWithGroupsResponse res = service.getWeeklyMenuWithGroups(storeId, wed);
        verify(menuGroupService, times(5)).getMenuGroups(eq(storeId), any(LocalDate.class));
        assertEquals(5, res.getDailyMenus().size());
    }

    @Test
    @DisplayName("getWeeklyMenuWithGroups: 요청 date 기준 pinned 반영")
    void getWeeklyMenuWithGroups_pinnedByRequestDate() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        LocalDate today = wed;
        LocalDate tomorrow = wed.plusDays(1);

        when(menuGroupService.getMenuGroups(eq(storeId), any(LocalDate.class)))
                .thenAnswer(inv -> {
                    LocalDate d = inv.getArgument(1);
                    boolean pinned = d.equals(tomorrow);
                    MenuGroupDto groupDto = MenuGroupDto.builder()
                            .id(1L)
                            .name("기본 메뉴")
                            .sortOrder(0)
                            .stock(10)
                            .capacity(10)
                            .menus(List.of("소보로빵"))
                            .menuItems(List.of(new MenuItemDto("소보로빵", pinned)))
                            .build();
                    return DailyMenuWithGroupsDto.builder()
                            .id(null)
                            .date(d)
                            .dayOfWeek(d.getDayOfWeek())
                            .isOpen(true)
                            .isHoliday(false)
                            .totalStock(10)
                            .groups(List.of(groupDto))
                            .build();
                });

        WeeklyMenuWithGroupsResponse res = service.getWeeklyMenuWithGroups(storeId, today);

        DailyMenuGroupResponse todayRes = res.getDailyMenus().stream()
                .filter(d -> d.getDate().equals(today))
                .findFirst()
                .orElseThrow();
        DailyMenuGroupResponse tomorrowRes = res.getDailyMenus().stream()
                .filter(d -> d.getDate().equals(tomorrow))
                .findFirst()
                .orElseThrow();

        assertFalse(todayRes.getGroups().get(0).getMenuItems().get(0).isPinned());
        assertTrue(tomorrowRes.getGroups().get(0).getMenuItems().get(0).isPinned());
    }

    @Test
    @DisplayName("getWeeklyMenuWithGroups: 월~금만 반환되는지 확인")
    void getWeeklyMenuWithGroups_onlyWeekdays() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        when(weeklyMenuRepository.findByStoreIdAndRangeWithDailyMenus(storeId, wed))
                .thenReturn(Optional.empty());
        when(menuGroupRepository.findByStoreIdWithStock(storeId))
                .thenReturn(List.of());

        WeeklyMenuWithGroupsResponse res = service.getWeeklyMenuWithGroups(storeId, wed);

        assertEquals(5, res.getDailyMenus().size());

        List<DayOfWeek> expectedDays = List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

        for (int i = 0; i < 5; i++) {
            assertEquals(expectedDays.get(i), res.getDailyMenus().get(i).getDayOfWeek());
        }

        List<DayOfWeek> resultDays = res.getDailyMenus().stream()
                .map(DailyMenuGroupResponse::getDayOfWeek)
                .toList();
        assertFalse(resultDays.contains(DayOfWeek.SATURDAY));
        assertFalse(resultDays.contains(DayOfWeek.SUNDAY));
    }
}
