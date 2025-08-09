package com._1000meal.menu.service;

import com._1000meal.menu.domain.Menu;
import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.WeeklyMenuRequest;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.repository.WeeklyMenuRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {


    private final StoreRepository storeRepository;
    private final WeeklyMenuRepository weeklyMenuRepository;

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


    // Service
    @Transactional(readOnly = true)
    public WeeklyMenuResponse getWeeklyMenu(Long storeId) {
        // 1) 매장 존재 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NoSuchElementException("Store not found: " + storeId));

        // 2) 이번 주의 시작/끝 계산 (월~일 기준)
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd   = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 3) 이번 주 WeeklyMenu + DailyMenu + Menu를 한 번에 로딩 (N+1 방지)
        WeeklyMenu weeklyMenu = weeklyMenuRepository
                .findByStoreIdAndRangeWithMenus(storeId, weekStart, weekEnd)
                .orElseThrow(() -> new NoSuchElementException("No weekly menu for current week: " + weekStart + " ~ " + weekEnd));

        // 4) DailyMenu를 날짜순 정렬 후 DTO 매핑 (빈 메뉴도 안전하게)
        List<WeeklyMenuResponse.DailyMenuResponse> dailyDtos = weeklyMenu.getDailyMenus().stream()
                .sorted(Comparator.comparing(DailyMenu::getDate))
                .map(daily -> {
                    List<String> menuNames = daily.getMenus() == null
                            ? Collections.emptyList()
                            : daily.getMenus().stream()
                            .map(Menu::getName)
                            .filter(Objects::nonNull)
                            .toList();

                    // dayOfWeek가 필드라면 동기화 보장용(선택)
                    DayOfWeek dow = daily.getDate() != null
                            ? daily.getDate().getDayOfWeek()
                            : daily.getDayOfWeek();

                    return WeeklyMenuResponse.DailyMenuResponse.builder()
                            .date(daily.getDate())
                            .dayOfWeek(dow)
                            .isOpen(daily.isOpen())
                            .menuNames(menuNames)
                            .build();
                })
                .toList();

        // 5) 최종 응답
        return WeeklyMenuResponse.builder()
                .storeId(store.getId())
                .startDate(weeklyMenu.getStartDate())
                .endDate(weeklyMenu.getEndDate())
                .dailyMenus(dailyDtos)
                .build();
    }


}
