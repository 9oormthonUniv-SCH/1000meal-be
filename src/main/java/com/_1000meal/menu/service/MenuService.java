package com._1000meal.menu.service;


import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.GroupDailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.menu.dto.DailyMenuGroupResponse;
import com._1000meal.menu.dto.MenuGroupResponse;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.dto.WeeklyMenuWithGroupsResponse;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.WeeklyMenuRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final StoreRepository storeRepository;
    private final WeeklyMenuRepository weeklyMenuRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final GroupDailyMenuRepository groupDailyMenuRepository;

    @Transactional(readOnly = true)
    public WeeklyMenuResponse getWeeklyMenu(Long storeId, LocalDate date) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        return weeklyMenuRepository.findByStoreIdAndRangeWithMenus(storeId, date)
                .map(weeklyMenu -> {
                    List<DailyMenuDto> dailyDtos = weeklyMenu.getDailyMenus().stream()
                            .sorted(Comparator.comparing(DailyMenu::getDate))
                            .map(dm -> {
                                DailyMenuDto dto = dm.toDto();
                                List<String> sorted = dto.getMenus().stream()
                                        .sorted()
                                        .toList();
                                dto.setMenus(sorted);
                                return dto;
                            })
                            .toList();

                    return WeeklyMenuResponse.builder()
                            .storeId(store.getId())
                            .startDate(weeklyMenu.getStartDate())
                            .endDate(weeklyMenu.getEndDate())
                            .dailyMenus(dailyDtos)
                            .build();
                })
                .orElseGet(() -> buildEmptyWeeklyMenu(store.getId(), date));
    }

    @Transactional
    public void ensureWeekDailyMenus(WeeklyMenu weekly) {
        LocalDate start = weekly.getStartDate();

        Set<LocalDate> existing = new HashSet<>(
                dailyMenuRepository.findDatesByWeeklyMenuId(weekly.getId())
        );

        List<DailyMenu> toCreate = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            LocalDate d = start.plusDays(i);
            if (!existing.contains(d)) {
                DailyMenu dm = DailyMenu.builder()
                        .weeklyMenu(weekly)
                        .date(d)
                        .build();
                toCreate.add(dm);
            }
        }

        if (!toCreate.isEmpty()) {
            List<DailyMenu> saved = dailyMenuRepository.saveAll(toCreate);

            Store store = weekly.getStore();
            for (DailyMenu dm : saved) {
                MenuGroup defaultGroup = MenuGroup.builder()
                        .store(store)
                        .dailyMenu(dm)
                        .name(store.getName())
                        .sortOrder(0)
                        .isDefault(true)
                        .build();
                defaultGroup.initializeStock(100);
                dm.addMenuGroup(defaultGroup);
                menuGroupRepository.save(defaultGroup);
            }
        }
    }

    private WeeklyMenuResponse buildEmptyWeeklyMenu(Long storeId, LocalDate refDate) {
        LocalDate start = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);

        List<DailyMenuDto> daily = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            LocalDate d = start.plusDays(i);
            daily.add(DailyMenuDto.builder()
                    .id(null)
                    .date(d)
                    .dayOfWeek(d.getDayOfWeek())
                    .stock(0)
                    .menus(List.of())
                    .isOpen(true)
                    .build());
        }

        return WeeklyMenuResponse.builder()
                .storeId(storeId)
                .startDate(start)
                .endDate(end)
                .dailyMenus(daily)
                .build();
    }

    @Transactional(readOnly = true)
    public WeeklyMenuWithGroupsResponse getWeeklyMenuWithGroups(Long storeId, LocalDate date) {
        log.info("[MENU][WEEKLY_GROUP] storeId={}, date={}", storeId, date);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        Optional<WeeklyMenu> weeklyMenuOpt = weeklyMenuRepository.findByStoreIdAndRangeWithDailyMenus(storeId, date);
        WeeklyMenu weeklyMenu = weeklyMenuOpt.orElse(null);

        Map<LocalDate, DailyMenu> dailyMenuByDate = weeklyMenu == null
                ? Map.of()
                : weeklyMenu.getDailyMenus().stream()
                        .collect(Collectors.toMap(DailyMenu::getDate, dm -> dm));

        List<MenuGroup> storeGroups = menuGroupRepository.findByStoreIdWithStock(storeId);
        List<Long> groupIds = storeGroups.stream().map(MenuGroup::getId).toList();

        Map<LocalDate, Map<Long, GroupDailyMenu>> dailyMenusByDateAndGroup = groupIds.isEmpty()
                ? Map.of()
                : groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(groupIds, weekStart, weekEnd).stream()
                        .collect(Collectors.groupingBy(
                                GroupDailyMenu::getDate,
                                Collectors.toMap(
                                        gdm -> gdm.getMenuGroup().getId(),
                                        gdm -> gdm
                                )
                        ));

        List<DailyMenuGroupResponse> dailyResponses = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            LocalDate d = weekStart.plusDays(i);
            DailyMenu dm = dailyMenuByDate.get(d);
            Map<Long, GroupDailyMenu> dailyMenuMap = dailyMenusByDateAndGroup.getOrDefault(d, Map.of());

            List<MenuGroupResponse> groupResponses = storeGroups.stream()
                    .map(group -> {
                        GroupDailyMenu gdm = dailyMenuMap.get(group.getId());
                        List<String> menus = gdm != null ? gdm.getMenuNames() : List.of();
                        return MenuGroupResponse.from(group, menus);
                    })
                    .toList();

            int totalStock = groupResponses.stream()
                    .mapToInt(gr -> gr.getStock() != null ? gr.getStock() : 0)
                    .sum();

            dailyResponses.add(DailyMenuGroupResponse.storeBased(
                    dm,
                    d,
                    groupResponses,
                    totalStock,
                    dm != null ? dm.isOpen() : true,
                    dm != null && dm.isHoliday()
            ));
        }

        log.info("[MENU][WEEKLY_GROUP] storeId={}, days={}", storeId, dailyResponses.size());

        return WeeklyMenuWithGroupsResponse.builder()
                .storeId(store.getId())
                .startDate(weekStart)
                .endDate(weekEnd)
                .dailyMenus(dailyResponses)
                .build();
    }

    private WeeklyMenuWithGroupsResponse buildEmptyWeeklyMenuWithGroups(Long storeId, LocalDate refDate) {
        LocalDate start = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);

        List<DailyMenuGroupResponse> dailyResponses = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            LocalDate d = start.plusDays(i);
            dailyResponses.add(DailyMenuGroupResponse.skeleton(d));
        }

        return WeeklyMenuWithGroupsResponse.builder()
                .storeId(storeId)
                .startDate(start)
                .endDate(end)
                .dailyMenus(dailyResponses)
                .build();
    }
}
