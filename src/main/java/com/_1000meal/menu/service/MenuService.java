package com._1000meal.menu.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.Menu;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuAddRequest;
import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.menu.dto.StockResponse;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.repository.MenuRepository;
import com._1000meal.menu.repository.WeeklyMenuRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import com._1000meal.global.error.code.MenuErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {


    private final StoreRepository storeRepository;
    private final WeeklyMenuRepository weeklyMenuRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final MenuRepository menuRepository;

    @Transactional(readOnly = false)
    public void addOrReplaceDailyMenu(Long storeId, DailyMenuAddRequest req) {
        // 1) 유효성
        if (req.getMenus() == null || req.getMenus().isEmpty()) {
            throw new CustomException(MenuErrorCode.MENU_EMPTY);
        }
        // 정제: trim, 공백 제거, 중복 제거
        List<String> cleaned = req.getMenus().stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        if (cleaned.isEmpty()) throw new CustomException(MenuErrorCode.MENU_EMPTY);

        // 2) 필수 엔티티 로드
        DailyMenu dm = dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, req.getDate())
                .orElseThrow(() -> new CustomException(MenuErrorCode.DAILY_MENU_NOT_FOUND));

        Long dailyMenuId = dm.getId();

        // 4) 메뉴 교체
        menuRepository.deleteByDailyMenuId(dailyMenuId);

        List<Menu> toInsert = cleaned.stream()
                .map(name -> {
                    Menu m = Menu.builder().name(name).build();
                    m.setDailyMenu(dm);
                    return m;
                })
                .toList();

        menuRepository.saveAll(toInsert);
    }

    @Transactional(readOnly = true)
    public DailyMenuDto getDailyMenu(Long storeId, LocalDate date) {

        DailyMenu dm = dailyMenuRepository.findDailyMenuByStoreIdAndDate(storeId, date)
                .orElseThrow(() -> new CustomException(MenuErrorCode.DAILY_MENU_NOT_FOUND));

        // 안정된 순서로 메뉴 반환 (id 오름차순)
        List<Menu> menus = menuRepository.findByDailyMenu_IdOrderByIdAsc(dm.getId());
        return dm.toDto();
    }

    @Transactional(readOnly = true)
    public WeeklyMenuResponse getWeeklyMenu(Long storeId) {
        // 1. 매장 존재 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        // 2. 오늘 날짜 기준
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 3. 주간 메뉴 가져 오기
        WeeklyMenu weeklyMenu = weeklyMenuRepository
                .findByStoreIdAndRangeWithMenus(storeId, today)
                .orElseThrow(() -> new CustomException(MenuErrorCode.WEEKLY_MENU_NOT_FOUND));

        // 4. DailyMenu 매핑
        List<DailyMenuDto> dailyDtos = weeklyMenu.getDailyMenus().stream()
                .map(DailyMenu::toDto)
                .collect(Collectors.toList());

        // 5. 최종 응답
        return WeeklyMenuResponse.builder()
                .storeId(store.getId())
                .startDate(weeklyMenu.getStartDate())
                .endDate(weeklyMenu.getEndDate())
                .dailyMenus(dailyDtos)
                .build();
    }


    @Transactional
    public StockResponse deductStock(Long menuId, Integer value) {
        DailyMenu dailyMenu = dailyMenuRepository.findById(menuId) // Lock?
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_NOT_FOUND));

        if(dailyMenu.getStock() < value) {
            throw new CustomException(MenuErrorCode.INSUFFICIENT_STOCK);
        }

        dailyMenu.deductStock(value);
        return new StockResponse(dailyMenu.getId(), dailyMenu.getStock());
    }

    @Transactional
    public StockResponse operationStock(Long menuId, Integer stock) {
        DailyMenu dailyMenu = dailyMenuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_NOT_FOUND));

        dailyMenu.updateStock(stock);
        return new StockResponse(dailyMenu.getId(), dailyMenu.getStock());
    }
}
