package com._1000meal.menu.service;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.Menu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.dto.*;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.domain.StockDeductResult;
import com._1000meal.menu.event.LowStock30Event;
import com._1000meal.menu.event.LowStockEvent;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuGroupStockRepository;
import com._1000meal.store.domain.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuGroupService {

    private final MenuGroupRepository menuGroupRepository;
    private final MenuGroupStockRepository stockRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 특정 매장/날짜의 메뉴 그룹 목록 조회
     */
    @Transactional(readOnly = true)
    public DailyMenuWithGroupsDto getMenuGroups(Long storeId, LocalDate date) {
        DailyMenu dailyMenu = dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date)
                .orElseThrow(() -> new CustomException(MenuErrorCode.DAILY_MENU_NOT_FOUND));

        List<MenuGroup> groups = menuGroupRepository.findByDailyMenuIdWithStockAndMenus(dailyMenu.getId());

        List<MenuGroupDto> groupDtos = groups.stream()
                .map(MenuGroupDto::from)
                .toList();

        return DailyMenuWithGroupsDto.from(dailyMenu, groupDtos);
    }

    /**
     * 그룹 재고 차감
     * 비관적 락으로 동시성 보장
     */
    @Transactional
    public MenuGroupStockResponse deductStock(Long groupId, DeductionUnit unit) {
        int value = unit.getValue();

        // 비관적 락으로 재고 조회
        MenuGroupStock stock = stockRepository.findByMenuGroupIdForUpdate(groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        int beforeStock = stock.getStock();
        StockDeductResult result = stock.deduct(value);
        int afterStock = stock.getStock();

        log.info("[STOCK][DEDUCT] groupId={}, before={}, after={}, unit={}",
                groupId, beforeStock, afterStock, unit.name());

        if (result.shouldNotify()) {
            // 그룹 정보와 매장 정보 조회
            MenuGroup group = menuGroupRepository.findByIdWithStore(groupId)
                    .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

            Store store = group.getDailyMenu().getWeeklyMenu().getStore();

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
    public MenuGroupDto createMenuGroup(Long storeId, LocalDate date, MenuGroupCreateRequest request) {
        DailyMenu dailyMenu = dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date)
                .orElseThrow(() -> new CustomException(MenuErrorCode.DAILY_MENU_NOT_FOUND));

        MenuGroup menuGroup = MenuGroup.builder()
                .dailyMenu(dailyMenu)
                .name(request.getName())
                .sortOrder(request.getSortOrderOrDefault())
                .build();

        menuGroup.initializeStock(request.getCapacityOrDefault());

        dailyMenu.addMenuGroup(menuGroup);
        menuGroupRepository.save(menuGroup);

        return MenuGroupDto.from(menuGroup);
    }

    /**
     * 그룹 내 메뉴 등록/교체 (전체 교체 방식)
     */
    @Transactional
    public MenuGroupDto updateMenusInGroup(Long groupId, MenuUpdateRequest request) {
        MenuGroup menuGroup = menuGroupRepository.findByIdWithMenus(groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        DailyMenu dailyMenu = menuGroup.getDailyMenu();

        List<String> cleaned = request.getMenus().stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (cleaned.isEmpty()) {
            throw new CustomException(MenuErrorCode.MENU_EMPTY);
        }

        List<Menu> newMenus = cleaned.stream()
                .map(name -> {
                    Menu m = Menu.builder().name(name).build();
                    m.setDailyMenu(dailyMenu);
                    return m;
                })
                .toList();

        menuGroup.replaceMenus(newMenus);

        return MenuGroupDto.from(menuGroup);
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
