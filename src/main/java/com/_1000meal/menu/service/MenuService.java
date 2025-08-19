package com._1000meal.menu.service;

import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.menu.dto.StockResponse;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.repository.DailyMenuRepository;
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

//    public WeeklyMenuResponse createWeeklyMenu(WeeklyMenuRequest request) {
//        Store store = storeRepository.findById(request.getStoreId())
//                .orElseThrow(() -> new RuntimeException("Store not found"));
//
//        WeeklyMenu weeklyMenu = WeeklyMenu.builder()
//                .store(store)
//                .build();
//
//        List<DailyMenu> dailyMenus = request.getDailyMenus().stream()
//                .map(dto -> DailyMenu.builder()
//                        .weeklyMenu(weeklyMenu)
//                        .date(dto.getDate())
//                        .dayOfWeek(dto.getDayOfWeek())
//                        .isOpen(dto.isOpen())
//                        .menuTexts(String.join(",", dto.getMenuNames()))
//                        .build())
//                .collect(Collectors.toList());
//
//        weeklyMenu.setDailyMenus(dailyMenus);
//        store.setWeeklyMenu(weeklyMenu); // 역방향 연관관계 설정 (optional)
//
//        WeeklyMenu saved = weeklyMenuRepository.save(weeklyMenu);
//
//        // 응답 변환
//        List<WeeklyMenuResponse.DailyMenuResponse> dailyDtos = saved.getDailyMenus().stream()
//                .map(d -> WeeklyMenuResponse.DailyMenuResponse.builder()
//                        .date(d.getDate())
//                        .dayOfWeek(d.getDayOfWeek())
//                        .isOpen(d.isOpen())
//                        .menuNames(d.getMenuTexts() == null ? new ArrayList<>() : Arrays.asList(d.getMenuTexts().split(",")))
//                        .build())
//                .collect(Collectors.toList());
//
//        return WeeklyMenuResponse.builder()
//                .storeId(store.getId())
//                .dailyMenus(dailyDtos)
//                .build();
//    }

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
