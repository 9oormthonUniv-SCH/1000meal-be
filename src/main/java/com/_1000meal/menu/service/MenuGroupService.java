package com._1000meal.menu.service;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.DefaultGroupMenu;
import com._1000meal.menu.domain.GroupDailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.dto.*;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.domain.StockDeductResult;
import com._1000meal.menu.event.LowStock30Event;
import com._1000meal.menu.event.LowStockEvent;
import com._1000meal.menu.event.WeeklyMenuUploadedEvent;
import com._1000meal.menu.repository.DefaultGroupMenuRepository;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import com._1000meal.store.dto.StoreTodayMenuDto;
import com._1000meal.store.dto.StoreTodayMenuGroupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuGroupService {

    private final MenuGroupRepository menuGroupRepository;
    private final MenuGroupStockRepository stockRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final GroupDailyMenuRepository groupDailyMenuRepository;
    private final DefaultGroupMenuRepository defaultGroupMenuRepository;
    private final StoreRepository storeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentAccountProvider currentAccountProvider;

    /**
     * 특정 매장/날짜의 메뉴 그룹 목록 조회
     */
    @Transactional
    public DailyMenuWithGroupsDto getMenuGroups(Long storeId, LocalDate date) {
        DailyMenu dailyMenu = dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date)
                .orElse(null);

        List<MenuGroup> groups = (dailyMenu != null)
                ? menuGroupRepository.findByDailyMenuIdWithStockAndMenus(dailyMenu.getId())
                : menuGroupRepository.findByStoreIdWithStock(storeId);

        boolean isOpen = dailyMenu != null ? dailyMenu.isOpen() : true;
        boolean isHoliday = dailyMenu != null && dailyMenu.isHoliday();

        List<Long> groupIds = groups.stream().map(MenuGroup::getId).toList();

        Map<Long, GroupDailyMenu> dailyMenusByGroupId = groupIds.isEmpty()
                ? Collections.emptyMap()
                : groupDailyMenuRepository.findByMenuGroupIdInAndDate(groupIds, date).stream()
                        .collect(Collectors.toMap(
                                gdm -> gdm.getMenuGroup().getId(),
                                gdm -> gdm
                        ));

        Map<Long, List<DefaultGroupMenu>> defaultMenusByGroupId = (isOpen && !isHoliday && !groupIds.isEmpty())
                ? defaultGroupMenuRepository.findApplicableByMenuGroupIdsAndDate(groupIds, date).stream()
                        .collect(Collectors.groupingBy(
                                rule -> rule.getMenuGroup().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ))
                : Collections.emptyMap();

        List<MenuGroupDto> groupDtos = groups.stream()
                .map(group -> {
                    GroupDailyMenu gdm = dailyMenusByGroupId.get(group.getId());

                    if (gdm == null && isOpen && !isHoliday) {
                        List<DefaultGroupMenu> defaults = defaultMenusByGroupId.getOrDefault(group.getId(), List.of());
                        if (!defaults.isEmpty()) {
                            List<String> names = defaults.stream()
                                    .map(DefaultGroupMenu::getMenuName)
                                    .distinct()
                                    .toList();
                            gdm = GroupDailyMenu.builder()
                                    .menuGroup(group)
                                    .date(date)
                                    .build();
                            gdm.replaceMenus(names);
                            groupDailyMenuRepository.save(gdm);
                            dailyMenusByGroupId.put(group.getId(), gdm);
                            log.debug("[DEFAULT_MENU][LAZY_MATERIALIZE] groupId={}, date={}, created=true, itemCount={}",
                                    group.getId(), date, names.size());
                        } else {
                            log.debug("[DEFAULT_MENU][LAZY_MATERIALIZE] groupId={}, date={}, skipped=no_default",
                                    group.getId(), date);
                        }
                    } else if (gdm != null) {
                        log.debug("[DEFAULT_MENU][LAZY_MATERIALIZE] groupId={}, date={}, skipped=already_exists",
                                group.getId(), date);
                    }

                    if (!isOpen || isHoliday) {
                        return MenuGroupDto.from(group, List.of(), List.of());
                    }

                    if (gdm == null) {
                        return MenuGroupDto.from(group, List.of(), List.of());
                    }

                    List<String> menus = gdm.getMenuNames();
                    List<DefaultGroupMenu> defaultMenus = defaultMenusByGroupId.getOrDefault(group.getId(), List.of());
                    Map<String, Boolean> pinnedByName = defaultMenus.stream()
                            .filter(dgm -> dgm.isPinnedOn(date))
                            .collect(Collectors.toMap(
                                    DefaultGroupMenu::getMenuName,
                                    dgm -> true,
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));

                    List<MenuItemDto> menuItems = menus.stream()
                            .map(name -> new MenuItemDto(name, pinnedByName.containsKey(name)))
                            .toList();
                    return MenuGroupDto.from(group, menus, menuItems);
                })
                .toList();

        int totalStock = groupDtos.stream()
                .mapToInt(g -> g.getStock() != null ? g.getStock() : 0)
                .sum();

        return DailyMenuWithGroupsDto.builder()
                .id(dailyMenu != null ? dailyMenu.getId() : null)
                .date(date)
                .dayOfWeek(date.getDayOfWeek())
                .isOpen(isOpen)
                .isHoliday(isHoliday)
                .totalStock(totalStock)
                .groups(groupDtos)
                .build();
    }

    /**
     * 여러 매장의 특정 날짜 메뉴 정보를 한 번에 조회 (store 목록용)
     */
    @Transactional(readOnly = true)
    public Map<Long, DailyMenuDto> getDailyMenuDtosForStores(List<Long> storeIds, LocalDate date) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Map.of();
        }

        List<DailyMenu> dailyMenus = dailyMenuRepository.findByStoreIdInAndDate(storeIds, date);
        Map<Long, DailyMenu> dailyMenuByStoreId = dailyMenus.stream()
                .collect(Collectors.toMap(
                        dm -> dm.getWeeklyMenu().getStore().getId(),
                        dm -> dm
                ));

        List<Long> storesWithoutDaily = storeIds.stream()
                .filter(id -> !dailyMenuByStoreId.containsKey(id))
                .toList();

        List<Long> dailyMenuIds = dailyMenus.stream()
                .map(DailyMenu::getId)
                .toList();

        Map<Long, List<MenuGroup>> groupsByDailyMenuId = dailyMenuIds.isEmpty()
                ? Map.of()
                : menuGroupRepository.findByDailyMenuIdsWithStockAndMenus(dailyMenuIds).stream()
                        .collect(Collectors.groupingBy(
                                mg -> mg.getDailyMenu().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        Map<Long, List<MenuGroup>> groupsByStoreId = storesWithoutDaily.isEmpty()
                ? Map.of()
                : menuGroupRepository.findByStoreIdInWithStock(storesWithoutDaily).stream()
                        .collect(Collectors.groupingBy(
                                mg -> mg.getStore().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<Long> groupIds = new ArrayList<>();
        groupsByDailyMenuId.values().forEach(list -> list.forEach(mg -> groupIds.add(mg.getId())));
        groupsByStoreId.values().forEach(list -> list.forEach(mg -> groupIds.add(mg.getId())));

        Map<Long, List<String>> menusByGroupId = groupIds.isEmpty()
                ? Map.of()
                : groupDailyMenuRepository.findByMenuGroupIdInAndDate(groupIds, date).stream()
                        .collect(Collectors.toMap(
                                gdm -> gdm.getMenuGroup().getId(),
                                GroupDailyMenu::getMenuNames,
                                (a, b) -> a
                        ));

        Map<Long, DailyMenuDto> result = new HashMap<>();
        for (Long storeId : storeIds) {
            DailyMenu dm = dailyMenuByStoreId.get(storeId);
            List<MenuGroup> groups = (dm != null)
                    ? groupsByDailyMenuId.getOrDefault(dm.getId(), List.of())
                    : groupsByStoreId.getOrDefault(storeId, List.of());

            List<MenuGroupResponseDto> groupDtos = groups.stream()
                    .map(group -> {
                        List<MenuResponseDto> menuDtos = menusByGroupId
                                .getOrDefault(group.getId(), List.of())
                                .stream()
                                .map(name -> MenuResponseDto.builder().id(null).name(name).build())
                                .toList();
                        return MenuGroupResponseDto.from(group, menuDtos);
                    })
                    .toList();

            List<String> flatMenus = groups.stream()
                    .flatMap(group -> menusByGroupId.getOrDefault(group.getId(), List.of()).stream())
                    .toList();

            int totalStock = groups.stream()
                    .mapToInt(g -> g.getStock() != null ? g.getStock().getStock() : 0)
                    .sum();

            boolean isOpen = dm != null ? dm.isOpen() : true;
            boolean isHoliday = dm != null && dm.isHoliday();
            Long dailyMenuId = dm != null ? dm.getId() : null;

            result.put(storeId, DailyMenuDto.builder()
                    .id(dailyMenuId)
                    .date(date)
                    .dayOfWeek(date.getDayOfWeek())
                    .isOpen(isOpen)
                    .isHoliday(isHoliday)
                    .stock(totalStock)
                    .menus(flatMenus)
                    .menuGroups(groupDtos)
                    .build());
        }

        return result;
    }

    /**
     * /stores 전용: 그룹 기준으로 오늘 메뉴를 배치 조회
     */
    @Transactional(readOnly = true)
    public Map<Long, StoreTodayMenuDto> getTodayMenuForStores(List<Long> storeIds, LocalDate date) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Map.of();
        }

        List<DailyMenu> dailyMenus = dailyMenuRepository.findByStoreIdInAndDate(storeIds, date);
        Map<Long, DailyMenu> dailyMenuByStoreId = dailyMenus.stream()
                .collect(Collectors.toMap(
                        dm -> dm.getWeeklyMenu().getStore().getId(),
                        dm -> dm
                ));

        List<Long> storesWithoutDaily = storeIds.stream()
                .filter(id -> !dailyMenuByStoreId.containsKey(id))
                .toList();

        List<Long> dailyMenuIds = dailyMenus.stream()
                .map(DailyMenu::getId)
                .toList();

        Map<Long, List<MenuGroup>> groupsByDailyMenuId = dailyMenuIds.isEmpty()
                ? Map.of()
                : menuGroupRepository.findByDailyMenuIdsWithStockAndMenus(dailyMenuIds).stream()
                        .collect(Collectors.groupingBy(
                                mg -> mg.getDailyMenu().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        Map<Long, List<MenuGroup>> groupsByStoreId = storesWithoutDaily.isEmpty()
                ? Map.of()
                : menuGroupRepository.findByStoreIdInWithStock(storesWithoutDaily).stream()
                        .collect(Collectors.groupingBy(
                                mg -> mg.getStore().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<Long> groupIds = new ArrayList<>();
        groupsByDailyMenuId.values().forEach(list -> list.forEach(mg -> groupIds.add(mg.getId())));
        groupsByStoreId.values().forEach(list -> list.forEach(mg -> groupIds.add(mg.getId())));

        Map<Long, List<String>> menusByGroupId = groupIds.isEmpty()
                ? Map.of()
                : groupDailyMenuRepository.findByMenuGroupIdInAndDate(groupIds, date).stream()
                        .collect(Collectors.toMap(
                                gdm -> gdm.getMenuGroup().getId(),
                                GroupDailyMenu::getMenuNames,
                                (a, b) -> a
                        ));

        Map<Long, StoreTodayMenuDto> result = new HashMap<>();
        for (Long storeId : storeIds) {
            DailyMenu dm = dailyMenuByStoreId.get(storeId);
            List<MenuGroup> groups = (dm != null)
                    ? groupsByDailyMenuId.getOrDefault(dm.getId(), List.of())
                    : groupsByStoreId.getOrDefault(storeId, List.of());

            List<StoreTodayMenuGroupDto> groupDtos = groups.stream()
                    .map(group -> {
                        List<MenuResponseDto> menuDtos = menusByGroupId
                                .getOrDefault(group.getId(), List.of())
                                .stream()
                                .map(name -> MenuResponseDto.builder().id(null).name(name).build())
                                .toList();
                        return StoreTodayMenuGroupDto.from(group, menuDtos);
                    })
                    .toList();

            boolean isOpen = dm != null ? dm.isOpen() : true;
            boolean isHoliday = dm != null && dm.isHoliday();
            Long dailyMenuId = dm != null ? dm.getId() : null;

            result.put(storeId, StoreTodayMenuDto.builder()
                    .id(dailyMenuId)
                    .date(date)
                    .dayOfWeek(date.getDayOfWeek())
                    .isOpen(isOpen)
                    .isHoliday(isHoliday)
                    .menuGroups(groupDtos)
                    .build());
        }

        return result;
    }

    /**
     * 그룹 재고 차감
     * 비관적 락으로 동시성 보장
     */
    @Transactional
    public MenuGroupStockResponse deductStock(Long groupId, DeductionUnit unit) {
        int value = unit.getValue();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        MenuGroup group = getAuthorizedGroup(groupId);

        // 비관적 락으로 재고 조회
        MenuGroupStock stock = stockRepository.findByMenuGroupIdForUpdate(groupId)
                .orElseGet(() -> createStockIfMissing(group, 0));

        int beforeStock = stock.getStock();
        StockDeductResult result = stock.deduct(value, today);
        int afterStock = stock.getStock();

        log.info("[STOCK][DEDUCT] groupId={}, before={}, after={}, unit={}",
                groupId, beforeStock, afterStock, unit.name());

        if (result.shouldNotify()) {
            Store store = group.getStore();

            // 30 임계치 하향 돌파 알림 이벤트
            if (result.notifyLowStock30()) {
                eventPublisher.publishEvent(new LowStock30Event(
                        store.getId(),
                        store.getName(),
                        groupId,
                        group.getName(),
                        afterStock
                ));
            }

            // 10 임계치 하향 돌파 알림 이벤트
            if (result.notifyLowStock10()) {
                eventPublisher.publishEvent(new LowStockEvent(
                        store.getId(),
                        store.getName(),
                        groupId,
                        group.getName(),
                        afterStock
                ));
            }
        }

        return new MenuGroupStockResponse(groupId, afterStock);
    }

    /**
     * 그룹 재고 직접 수정
     */
    @Transactional
    public MenuGroupStockResponse updateStock(Long groupId, int newStock) {
        MenuGroup group = getAuthorizedGroup(groupId);
        MenuGroupStock stock = getOrCreateStock(group);

        stock.updateStock(newStock);

        log.info("[STOCK][UPDATE] groupId={}, newStock={}", groupId, newStock);

        return new MenuGroupStockResponse(groupId, stock.getStock());
    }

    @Transactional
    public MenuGroupStockResponse deductStockForStore(Long storeId, Long groupId, DeductionUnit unit) {
        MenuGroup group = getAuthorizedGroupForStore(storeId, groupId);
        int value = unit.getValue();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        MenuGroupStock stock = stockRepository.findByMenuGroupIdForUpdate(groupId)
                .orElseGet(() -> createStockIfMissing(group, 0));

        int beforeStock = stock.getStock();
        StockDeductResult result = stock.deduct(value, today);
        int afterStock = stock.getStock();

        log.info("[STOCK][DEDUCT] groupId={}, before={}, after={}, unit={}",
                groupId, beforeStock, afterStock, unit.name());

        if (result.shouldNotify()) {
            Store store = group.getStore();

            if (result.notifyLowStock30()) {
                eventPublisher.publishEvent(new LowStock30Event(
                        store.getId(),
                        store.getName(),
                        groupId,
                        group.getName(),
                        afterStock
                ));
            }

            if (result.notifyLowStock10()) {
                eventPublisher.publishEvent(new LowStockEvent(
                        store.getId(),
                        store.getName(),
                        groupId,
                        group.getName(),
                        afterStock
                ));
            }
        }

        return new MenuGroupStockResponse(groupId, afterStock);
    }

    @Transactional
    public MenuGroupStockResponse updateStockForStore(Long storeId, Long groupId, int newStock) {
        MenuGroup group = getAuthorizedGroupForStore(storeId, groupId);
        MenuGroupStock stock = getOrCreateStock(group);

        stock.updateStock(newStock);

        log.info("[STOCK][UPDATE] groupId={}, newStock={}", groupId, newStock);

        return new MenuGroupStockResponse(groupId, stock.getStock());
    }

    /**
     * 메뉴 그룹 생성 (그룹만 생성, 메뉴는 별도 API)
     */
    @Transactional
    public MenuGroupDto createMenuGroup(Long storeId, MenuGroupCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new CustomException(MenuErrorCode.INVALID_MENU_NAME);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        MenuGroup menuGroup = MenuGroup.builder()
                .store(store)
                .name(request.getName())
                .sortOrder(request.getSortOrderOrDefault())
                .isDefault(false)
                .build();

        menuGroup.initializeStock(request.getCapacityOrDefault());
        menuGroupRepository.save(menuGroup);

        return MenuGroupDto.from(menuGroup);
    }

    /**
     * 그룹 내 메뉴 등록/교체 (전체 교체 방식)
     * (groupId, date) 조합으로 GroupDailyMenu를 upsert
     */
    @Transactional
    public GroupDailyMenuResponse updateMenusInGroup(Long groupId, LocalDate date, MenuUpdateRequest request) {
        MenuGroup menuGroup = getAuthorizedGroup(groupId);
        return upsertGroupDailyMenu(menuGroup, date, request);
    }

    @Transactional
    public GroupDailyMenuResponse updateMenusInGroupForStore(
            Long storeId,
            Long groupId,
            LocalDate date,
            MenuUpdateRequest request
    ) {
        MenuGroup menuGroup = getAuthorizedGroupForStore(storeId, groupId);
        GroupDailyMenuResponse response = upsertGroupDailyMenu(menuGroup, date, request);

        Map<Long, Boolean> filledByGroup = isWeeklyMenuFilledByGroup(storeId, date);
        if (filledByGroup.getOrDefault(groupId, false)) {
            LocalDate weekStart = getWeekStart(date);
            String weekKey = weekStart.format(DateTimeFormatter.ISO_DATE);
            eventPublisher.publishEvent(
                    new WeeklyMenuUploadedEvent(storeId, List.of(groupId), weekKey, weekStart)
            );
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<MenuGroupAdminResponse> getMenuGroupsForAdmin(Long storeId) {
        return menuGroupRepository.findByStoreIdOrderBySortOrderAscIdAsc(storeId).stream()
                .map(MenuGroupAdminResponse::from)
                .toList();
    }

    private GroupDailyMenuResponse upsertGroupDailyMenu(
            MenuGroup menuGroup,
            LocalDate date,
            MenuUpdateRequest request
    ) {
        List<String> cleaned = request.getMenus().stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (cleaned.isEmpty()) {
            throw new CustomException(MenuErrorCode.MENU_EMPTY);
        }

        Long groupId = menuGroup.getId();
        GroupDailyMenu groupDailyMenu = groupDailyMenuRepository
                .findByMenuGroupIdAndDate(groupId, date)
                .orElseGet(() -> GroupDailyMenu.builder()
                        .menuGroup(menuGroup)
                        .date(date)
                        .build());

        groupDailyMenu.replaceMenus(cleaned);
        groupDailyMenuRepository.save(groupDailyMenu);

        return GroupDailyMenuResponse.from(groupDailyMenu);
    }

    private MenuGroup getAuthorizedGroup(Long groupId) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        MenuGroup menuGroup = menuGroupRepository.findByIdAndStoreId(groupId, accountStoreId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        return menuGroup;
    }

    private MenuGroup getAuthorizedGroupForStore(Long storeId, Long groupId) {
        MenuGroup menuGroup = menuGroupRepository.findByIdAndStoreId(groupId, storeId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        return menuGroup;
    }

    private MenuGroupStock getOrCreateStock(MenuGroup group) {
        return stockRepository.findByMenuGroupId(group.getId())
                .orElseGet(() -> createStockIfMissing(group, 0));
    }

    private MenuGroupStock createStockIfMissing(MenuGroup group, int capacity) {
        if (group.getStock() == null) {
            group.initializeStock(capacity);
        }
        return stockRepository.save(group.getStock());
    }

    @Transactional(readOnly = true)
    public boolean isWeeklyMenuFilled(Long storeId, LocalDate anyDateInWeek) {
        LocalDate weekStart = getWeekStart(anyDateInWeek);
        LocalDate weekEnd = weekStart.plusDays(4);

        List<MenuGroup> groups = menuGroupRepository.findByStoreIdOrderBySortOrderAscIdAsc(storeId);
        if (groups.isEmpty()) {
            return false;
        }

        List<Long> groupIds = groups.stream().map(MenuGroup::getId).toList();
        List<GroupDailyMenu> groupDailyMenus =
                groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(groupIds, weekStart, weekEnd);

        Set<LocalDate> filledDates = new HashSet<>();
        for (GroupDailyMenu gdm : groupDailyMenus) {
            if (gdm.getMenuNames() != null && !gdm.getMenuNames().isEmpty()) {
                filledDates.add(gdm.getDate());
            }
        }

        for (int i = 0; i < 5; i++) {
            if (!filledDates.contains(weekStart.plusDays(i))) {
                return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> isWeeklyMenuFilledByGroup(Long storeId, LocalDate anyDateInWeek) {
        LocalDate weekStart = getWeekStart(anyDateInWeek);
        LocalDate weekEnd = weekStart.plusDays(4);

        List<MenuGroup> groups = menuGroupRepository.findByStoreIdOrderBySortOrderAscIdAsc(storeId);
        if (groups.isEmpty()) {
            return Map.of();
        }

        List<Long> groupIds = groups.stream().map(MenuGroup::getId).toList();
        List<GroupDailyMenu> groupDailyMenus =
                groupDailyMenuRepository.findByMenuGroupIdInAndDateBetween(groupIds, weekStart, weekEnd);

        Map<Long, Set<LocalDate>> filledByGroup = new HashMap<>();
        for (GroupDailyMenu gdm : groupDailyMenus) {
            if (gdm.getMenuNames() != null && !gdm.getMenuNames().isEmpty()) {
                filledByGroup
                        .computeIfAbsent(gdm.getMenuGroup().getId(), k -> new HashSet<>())
                        .add(gdm.getDate());
            }
        }

        Map<Long, Boolean> result = new HashMap<>();
        for (Long groupId : groupIds) {
            Set<LocalDate> dates = filledByGroup.getOrDefault(groupId, Set.of());
            boolean ok = true;
            for (int i = 0; i < 5; i++) {
                if (!dates.contains(weekStart.plusDays(i))) {
                    ok = false;
                    break;
                }
            }
            result.put(groupId, ok);
        }
        return result;
    }

    private LocalDate getWeekStart(LocalDate date) {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * 메뉴 그룹 삭제 (기본 그룹은 삭제 불가)
     */
    @Transactional
    public void deleteMenuGroup(Long groupId) {
        MenuGroup menuGroup = menuGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        if (menuGroup.isDefault()) {
            throw new CustomException(MenuErrorCode.CANNOT_DELETE_DEFAULT_GROUP);
        }

        menuGroupRepository.delete(menuGroup);
    }

}
