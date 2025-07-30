package com._1000meal.menu.service;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {


    private final StoreRepository storeRepository;
    private final WeeklyMenuRepository weeklyMenuRepository;

    public WeeklyMenuResponse createWeeklyMenu(WeeklyMenuRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        WeeklyMenu weeklyMenu = WeeklyMenu.builder()
                .store(store)
                .build();

        List<DailyMenu> dailyMenus = request.getDailyMenus().stream()
                .map(dto -> DailyMenu.builder()
                        .weeklyMenu(weeklyMenu)
                        .date(dto.getDate())
                        .dayOfWeek(dto.getDayOfWeek())
                        .isOpen(dto.isOpen())
                        .menuTexts(String.join(",", dto.getMenuNames()))
                        .build())
                .collect(Collectors.toList());

        weeklyMenu.setDailyMenus(dailyMenus);
        store.setWeeklyMenu(weeklyMenu); // 역방향 연관관계 설정 (optional)

        WeeklyMenu saved = weeklyMenuRepository.save(weeklyMenu);

        // 응답 변환
        List<WeeklyMenuResponse.DailyMenuResponse> dailyDtos = saved.getDailyMenus().stream()
                .map(d -> WeeklyMenuResponse.DailyMenuResponse.builder()
                        .date(d.getDate())
                        .dayOfWeek(d.getDayOfWeek())
                        .isOpen(d.isOpen())
                        .menuNames(d.getMenuTexts() == null ? new ArrayList<>() : Arrays.asList(d.getMenuTexts().split(",")))
                        .build())
                .collect(Collectors.toList());

        return WeeklyMenuResponse.builder()
                .storeId(store.getId())
                .dailyMenus(dailyDtos)
                .build();
    }


    @Transactional(readOnly = true)
    public WeeklyMenuResponse getWeeklyMenu(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        WeeklyMenu weeklyMenu = store.getWeeklyMenu();
        if (weeklyMenu == null) {
            throw new RuntimeException("No weekly menu found for this store");
        }

        List<WeeklyMenuResponse.DailyMenuResponse> dailyDtos = weeklyMenu.getDailyMenus().stream()
                .map(daily -> WeeklyMenuResponse.DailyMenuResponse.builder()
                        .date(daily.getDate())
                        .dayOfWeek(daily.getDayOfWeek())
                        .isOpen(daily.isOpen())
                        .menuNames(daily.getMenuTexts() == null || daily.getMenuTexts().isBlank()
                                ? new ArrayList<>()
                                : Arrays.asList(daily.getMenuTexts().split(",")))
                        .build())
                .collect(Collectors.toList());

        return WeeklyMenuResponse.builder()
                .storeId(storeId)
                .dailyMenus(dailyDtos)
                .build();
    }

}
