package com._1000meal.store.service;

import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.Menu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.menu.dto.MenuGroupResponseDto;
import com._1000meal.menu.dto.MenuResponseDto;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuRepository;
import com._1000meal.menu.repository.WeeklyMenuRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.dto.StoreDetailedResponse;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoreDetailMenuGroupIT {

    @Autowired private StoreService storeService;
    @Autowired private StoreRepository storeRepository;
    @Autowired private WeeklyMenuRepository weeklyMenuRepository;
    @Autowired private DailyMenuRepository dailyMenuRepository;
    @Autowired private MenuGroupRepository menuGroupRepository;
    @Autowired private MenuRepository menuRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("getStoreDetail: dailyMenus.menuGroups에 그룹+메뉴가 포함되고 legacy menus는 menu_group_id NULL만 유지")
    void getStoreDetail_includesMenuGroupsAndLegacyMenus() {
        LocalDate today = LocalDate.now(KST);
        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);

        Store store = storeRepository.save(Store.builder()
                .name("store")
                .address("addr")
                .phone("010-0000-0000")
                .description("desc")
                .isOpen(true)
                .remain(10)
                .build());

        WeeklyMenu weeklyMenu = weeklyMenuRepository.save(WeeklyMenu.builder()
                .store(store)
                .startDate(start)
                .endDate(end)
                .build());

        DailyMenu dailyMenu = dailyMenuRepository.save(DailyMenu.builder()
                .weeklyMenu(weeklyMenu)
                .date(today)
                .build());

        MenuGroup group1 = menuGroupRepository.save(MenuGroup.builder()
                .store(store)
                .dailyMenu(dailyMenu)
                .name("groupA")
                .sortOrder(1)
                .isDefault(true)
                .build());

        MenuGroup group2 = menuGroupRepository.save(MenuGroup.builder()
                .store(store)
                .dailyMenu(dailyMenu)
                .name("groupB")
                .sortOrder(2)
                .isDefault(false)
                .build());

        Menu menu1 = Menu.builder().name("menu-1").build();
        menu1.setDailyMenu(dailyMenu);
        menu1.setMenuGroup(group1);

        Menu menu2 = Menu.builder().name("menu-2").build();
        menu2.setDailyMenu(dailyMenu);
        menu2.setMenuGroup(group1);

        Menu menu3 = Menu.builder().name("menu-3").build();
        menu3.setDailyMenu(dailyMenu);
        menu3.setMenuGroup(group2);

        Menu legacy = Menu.builder().name("legacy").build();
        legacy.setDailyMenu(dailyMenu);

        menuRepository.saveAll(List.of(menu1, menu2, menu3, legacy));

        StoreDetailedResponse response = storeService.getStoreDetail(store.getId());
        WeeklyMenuResponse weekly = response.getWeeklyMenuResponse();

        DailyMenuDto target = weekly.getDailyMenus().stream()
                .filter(dm -> today.equals(dm.getDate()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, target.getMenuGroups().size());

        MenuGroupResponseDto first = target.getMenuGroups().get(0);
        MenuGroupResponseDto second = target.getMenuGroups().get(1);

        assertEquals("groupA", first.getName());
        assertEquals(1, first.getSortOrder());
        assertTrue(first.isDefault());
        assertEquals(List.of("menu-1", "menu-2"),
                first.getMenus().stream().map(MenuResponseDto::getName).toList());

        assertEquals("groupB", second.getName());
        assertEquals(2, second.getSortOrder());
        assertFalse(second.isDefault());
        assertEquals(List.of("menu-3"),
                second.getMenus().stream().map(MenuResponseDto::getName).toList());

        assertEquals(List.of("legacy"), target.getMenus());
    }
}
