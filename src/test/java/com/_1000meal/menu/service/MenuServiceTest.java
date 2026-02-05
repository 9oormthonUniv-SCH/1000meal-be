package com._1000meal.menu.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.GroupDailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuGroupResponse;
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
        when(weeklyMenuRepository.findByStoreIdAndRangeWithDailyMenus(storeId, wed)).thenReturn(Optional.empty());

        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.getStock()).thenReturn(80);
        when(stock.getCapacity()).thenReturn(100);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(1L);
        when(group.getName()).thenReturn("기본 메뉴");
        when(group.getStock()).thenReturn(stock);

        when(menuGroupRepository.findByDailyMenuIdsWithStockAndMenus(List.of(10L)))
                .thenReturn(List.of(group));
        when(groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(List.of(1L), weekStart, weekEnd))
                .thenReturn(List.of());

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
        assertEquals(1, firstDay.getGroups().size());
        assertEquals(80, firstDay.getTotalStock());
    }

    @Test
    @DisplayName("getWeeklyMenuWithGroups: 주간 메뉴 존재하는 경우 그룹과 함께 반환")
    void getWeeklyMenuWithGroups_whenExists_returnsWithGroups() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        WeeklyMenu weeklyMenu = mock(WeeklyMenu.class);

        DailyMenu dm = mock(DailyMenu.class);
        when(dm.getId()).thenReturn(10L);
        when(dm.getDate()).thenReturn(wed);
        when(dm.isOpen()).thenReturn(true);
        when(dm.isHoliday()).thenReturn(false);

        Set<DailyMenu> dailyMenus = new LinkedHashSet<>();
        dailyMenus.add(dm);
        when(weeklyMenu.getDailyMenus()).thenReturn(dailyMenus);

        when(weeklyMenuRepository.findByStoreIdAndRangeWithDailyMenus(storeId, wed))
                .thenReturn(Optional.of(weeklyMenu));

        MenuGroupStock stock = mock(MenuGroupStock.class);
        when(stock.getStock()).thenReturn(90);
        when(stock.getCapacity()).thenReturn(100);

        MenuGroup group = mock(MenuGroup.class);
        when(group.getId()).thenReturn(1L);
        when(group.getName()).thenReturn("향설 1관");
        when(group.getStock()).thenReturn(stock);
        when(group.getSortOrder()).thenReturn(0);

        when(menuGroupRepository.findByStoreIdWithStock(storeId))
                .thenReturn(List.of(group));

        GroupDailyMenu groupDailyMenu = mock(GroupDailyMenu.class);
        when(groupDailyMenu.getDate()).thenReturn(wed);
        when(groupDailyMenu.getMenuGroup()).thenReturn(group);
        when(groupDailyMenu.getMenuNames()).thenReturn(List.of("떡볶이"));

        when(groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(List.of(1L), weekStart, weekEnd))
                .thenReturn(List.of(groupDailyMenu));

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
    }

    @Test
    @DisplayName("getWeeklyMenuWithGroups: 그룹 2개 이상인 경우 모두 반환")
    void getWeeklyMenuWithGroups_multipleGroups() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        WeeklyMenu weeklyMenu = mock(WeeklyMenu.class);

        DailyMenu dm = mock(DailyMenu.class);
        when(dm.getId()).thenReturn(10L);
        when(dm.getDate()).thenReturn(wed);
        when(dm.isOpen()).thenReturn(true);
        when(dm.isHoliday()).thenReturn(false);

        Set<DailyMenu> dailyMenus = new LinkedHashSet<>();
        dailyMenus.add(dm);
        when(weeklyMenu.getDailyMenus()).thenReturn(dailyMenus);

        when(weeklyMenuRepository.findByStoreIdAndRangeWithDailyMenus(storeId, wed))
                .thenReturn(Optional.of(weeklyMenu));

        MenuGroupStock stock1 = mock(MenuGroupStock.class);
        when(stock1.getStock()).thenReturn(90);
        when(stock1.getCapacity()).thenReturn(100);

        MenuGroup group1 = mock(MenuGroup.class);
        when(group1.getId()).thenReturn(1L);
        when(group1.getName()).thenReturn("향설 1관");
        when(group1.getStock()).thenReturn(stock1);
        when(group1.getSortOrder()).thenReturn(0);

        MenuGroupStock stock2 = mock(MenuGroupStock.class);
        when(stock2.getStock()).thenReturn(50);
        when(stock2.getCapacity()).thenReturn(50);

        MenuGroup group2 = mock(MenuGroup.class);
        when(group2.getId()).thenReturn(2L);
        when(group2.getName()).thenReturn("국밥 세트");
        when(group2.getStock()).thenReturn(stock2);
        when(group2.getSortOrder()).thenReturn(1);

        when(menuGroupRepository.findByDailyMenuIdsWithStockAndMenus(List.of(10L)))
                .thenReturn(List.of(group1, group2));

        when(groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(List.of(1L, 2L), weekStart, weekEnd))
                .thenReturn(List.of());

        WeeklyMenuWithGroupsResponse res = service.getWeeklyMenuWithGroups(storeId, wed);

        DailyMenuGroupResponse wedResponse = res.getDailyMenus().stream()
                .filter(d -> d.getDate().equals(wed))
                .findFirst()
                .orElseThrow();

        assertEquals(2, wedResponse.getGroups().size());
        assertEquals("향설 1관", wedResponse.getGroups().get(0).getName());
        assertEquals("국밥 세트", wedResponse.getGroups().get(1).getName());
        assertEquals(140, wedResponse.getTotalStock());
    }

    @Test
    @DisplayName("getWeeklyMenuWithGroups: GroupDailyMenu 기반으로 주간 메뉴가 채워짐")
    void getWeeklyMenuWithGroups_groupDailyMenusApplied() {
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        WeeklyMenu weeklyMenu = mock(WeeklyMenu.class);

        DailyMenu dm = mock(DailyMenu.class);
        when(dm.getId()).thenReturn(10L);
        when(dm.getDate()).thenReturn(wed);
        when(dm.isOpen()).thenReturn(true);
        when(dm.isHoliday()).thenReturn(false);

        Set<DailyMenu> dailyMenus = new LinkedHashSet<>();
        dailyMenus.add(dm);
        when(weeklyMenu.getDailyMenus()).thenReturn(dailyMenus);

        when(weeklyMenuRepository.findByStoreIdAndRangeWithDailyMenus(storeId, wed))
                .thenReturn(Optional.of(weeklyMenu));

        MenuGroupStock stock1 = mock(MenuGroupStock.class);
        when(stock1.getStock()).thenReturn(30);
        when(stock1.getCapacity()).thenReturn(50);

        MenuGroup group1 = mock(MenuGroup.class);
        when(group1.getId()).thenReturn(1L);
        when(group1.getName()).thenReturn("그룹1");
        when(group1.getStock()).thenReturn(stock1);
        when(group1.getSortOrder()).thenReturn(0);

        MenuGroupStock stock2 = mock(MenuGroupStock.class);
        when(stock2.getStock()).thenReturn(20);
        when(stock2.getCapacity()).thenReturn(20);

        MenuGroup group2 = mock(MenuGroup.class);
        when(group2.getId()).thenReturn(2L);
        when(group2.getName()).thenReturn("그룹2");
        when(group2.getStock()).thenReturn(stock2);
        when(group2.getSortOrder()).thenReturn(1);

        when(menuGroupRepository.findByDailyMenuIdsWithStockAndMenus(List.of(10L)))
                .thenReturn(List.of(group1, group2));

        GroupDailyMenu gdm1 = mock(GroupDailyMenu.class);
        when(gdm1.getDate()).thenReturn(wed);
        when(gdm1.getMenuGroup()).thenReturn(group1);
        when(gdm1.getMenuNames()).thenReturn(List.of("떡볶이", "순대"));

        GroupDailyMenu gdm2 = mock(GroupDailyMenu.class);
        when(gdm2.getDate()).thenReturn(wed);
        when(gdm2.getMenuGroup()).thenReturn(group2);
        when(gdm2.getMenuNames()).thenReturn(List.of("김밥"));

        when(groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(List.of(1L, 2L), weekStart, weekEnd))
                .thenReturn(List.of(gdm1, gdm2));

        WeeklyMenuWithGroupsResponse res = service.getWeeklyMenuWithGroups(storeId, wed);

        DailyMenuGroupResponse wedResponse = res.getDailyMenus().stream()
                .filter(d -> d.getDate().equals(wed))
                .findFirst()
                .orElseThrow();

        assertEquals(2, wedResponse.getGroups().size());
        assertEquals(List.of("떡볶이", "순대"), wedResponse.getGroups().get(0).getMenus());
        assertEquals(List.of("김밥"), wedResponse.getGroups().get(1).getMenus());
        assertEquals(50, wedResponse.getTotalStock());
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
