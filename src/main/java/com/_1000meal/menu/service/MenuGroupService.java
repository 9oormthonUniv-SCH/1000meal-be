package com._1000meal.menu.service;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.GroupDailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.dto.*;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.domain.StockDeductResult;
import com._1000meal.menu.event.LowStock30Event;
import com._1000meal.menu.event.LowStockEvent;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuGroupService {

    private final MenuGroupRepository menuGroupRepository;
    private final MenuGroupStockRepository stockRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final GroupDailyMenuRepository groupDailyMenuRepository;
    private final StoreRepository storeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentAccountProvider currentAccountProvider;

    /**
     * 특정 매장/날짜의 메뉴 그룹 목록 조회
     */
    @Transactional(readOnly = true)
    public DailyMenuWithGroupsDto getMenuGroups(Long storeId, LocalDate date) {
        DailyMenu dailyMenu = dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date)
                .orElse(null);

        List<MenuGroup> groups = (dailyMenu != null)
                ? menuGroupRepository.findByDailyMenuIdWithStockAndMenus(dailyMenu.getId())
                : menuGroupRepository.findByStoreIdWithStock(storeId);

        List<Long> groupIds = groups.stream().map(MenuGroup::getId).toList();

        Map<Long, GroupDailyMenu> dailyMenusByGroupId = groupIds.isEmpty()
                ? Collections.emptyMap()
                : groupDailyMenuRepository.findByMenuGroupIdInAndDate(groupIds, date).stream()
                        .collect(Collectors.toMap(
                                gdm -> gdm.getMenuGroup().getId(),
                                gdm -> gdm
                        ));

        List<MenuGroupDto> groupDtos = groups.stream()
                .map(group -> {
                    GroupDailyMenu gdm = dailyMenusByGroupId.get(group.getId());
                    List<String> menus = gdm != null ? gdm.getMenuNames() : List.of();
                    return MenuGroupDto.from(group, menus);
                })
                .toList();

        int totalStock = groupDtos.stream()
                .mapToInt(g -> g.getStock() != null ? g.getStock() : 0)
                .sum();

        return DailyMenuWithGroupsDto.builder()
                .id(dailyMenu != null ? dailyMenu.getId() : null)
                .date(date)
                .dayOfWeek(date.getDayOfWeek())
                .isOpen(dailyMenu != null ? dailyMenu.isOpen() : true)
                .isHoliday(dailyMenu != null && dailyMenu.isHoliday())
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

        List<Long> storesWithDaily = dailyMenuByStoreId.keySet().stream().toList();
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
     * 그룹 재고 차감
     * 비관적 락으로 동시성 보장
     */
    @Transactional
    public MenuGroupStockResponse deductStock(Long groupId, DeductionUnit unit) {
        int value = unit.getValue();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 비관적 락으로 재고 조회
        MenuGroupStock stock = stockRepository.findByMenuGroupIdForUpdate(groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        int beforeStock = stock.getStock();
        StockDeductResult result = stock.deduct(value, today);
        int afterStock = stock.getStock();

        log.info("[STOCK][DEDUCT] groupId={}, before={}, after={}, unit={}",
                groupId, beforeStock, afterStock, unit.name());

        if (result.shouldNotify()) {
            // 그룹 정보와 매장 정보 조회
            MenuGroup group = menuGroupRepository.findByIdWithStore(groupId)
                    .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

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
        MenuGroupStock stock = stockRepository.findByMenuGroupId(groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

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
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        MenuGroup menuGroup = menuGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        Long groupStoreId = menuGroup.getStore() != null ? menuGroup.getStore().getId() : null;
        if (groupStoreId == null || !groupStoreId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return upsertGroupDailyMenu(menuGroup, date, request);
    }

    @Transactional
    public GroupDailyMenuResponse updateMenusInGroupForStore(
            Long storeId,
            Long groupId,
            LocalDate date,
            MenuUpdateRequest request
    ) {
        MenuGroup menuGroup = menuGroupRepository.findByIdAndStoreId(groupId, storeId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        return upsertGroupDailyMenu(menuGroup, date, request);
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
