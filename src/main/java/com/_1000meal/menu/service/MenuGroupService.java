package com._1000meal.menu.service;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.Menu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuGroupStock;
import com._1000meal.menu.dto.*;
import com._1000meal.menu.enums.DeductionUnit;
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
        boolean shouldNotify = stock.deduct(value);
        int afterStock = stock.getStock();

        log.info("[STOCK][DEDUCT] groupId={}, before={}, after={}, unit={}",
                groupId, beforeStock, afterStock, unit.name());

        if (shouldNotify) {
            // 그룹 정보와 매장 정보 조회
            MenuGroup group = menuGroupRepository.findByIdWithStore(groupId)
                    .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

            Store store = group.getDailyMenu().getWeeklyMenu().getStore();

            // 트랜잭션 커밋 후 알림 발송을 위해 이벤트 발행
            eventPublisher.publishEvent(new LowStockEvent(
                    store.getId(),
                    store.getName(),
                    groupId,
                    group.getName(),
                    afterStock
            ));
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
     * 메뉴 그룹 생성
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

        // 메뉴 추가
        if (request.getMenus() != null && !request.getMenus().isEmpty()) {
            for (String menuName : request.getMenus()) {
                Menu menu = Menu.builder().name(menuName).build();
                menu.setDailyMenu(dailyMenu);
                menuGroup.addMenu(menu);
            }
        }

        dailyMenu.addMenuGroup(menuGroup);
        menuGroupRepository.save(menuGroup);

        return MenuGroupDto.from(menuGroup);
    }

    /**
     * 메뉴 그룹 삭제
     */
    @Transactional
    public void deleteMenuGroup(Long groupId) {
        MenuGroup menuGroup = menuGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        menuGroupRepository.delete(menuGroup);
    }
}
