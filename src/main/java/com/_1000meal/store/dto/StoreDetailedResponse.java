package com._1000meal.store.dto;

import com._1000meal.menu.domain.DailyMenu;
import com._1000meal.menu.domain.WeeklyMenu;
import com._1000meal.menu.dto.DailyMenuDto;
import com._1000meal.store.domain.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class StoreDetailedResponse {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isOpen;
    private int remain;
    private String hours;
    private double lat;
    private double lng;

    private List<DailyMenuDto> dailyMenus;

    public static StoreDetailedResponse from(Store store, WeeklyMenu weeklyMenu) {
        List<DailyMenuDto> dailyMenuDtos = List.of();

        if (weeklyMenu != null && weeklyMenu.getDailyMenus() != null) {
            Map<DayOfWeek, DailyMenu> dailyMenuMap = weeklyMenu.getDailyMenus().stream()
                    .collect(Collectors.toMap(DailyMenu::getDayOfWeek, dm -> dm));

            dailyMenuDtos = Arrays.stream(DayOfWeek.values())
                    .filter(dow -> dow.getValue() >= 1 && dow.getValue() <= 5) // 월~금
                    .map(dow -> {
                        DailyMenu dailyMenu = dailyMenuMap.get(dow);
                        if (dailyMenu != null) {
                            return DailyMenuDto.from(dailyMenu);
                        } else {
                            return DailyMenuDto.empty(dow); // 기본값
                        }
                    })
                    .toList();
        }

        return StoreDetailedResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .description(store.getDescription())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .isOpen(store.isOpen())
                .remain(store.getRemain())
                .hours(store.getHours())
                .lat(store.getLat())
                .lng(store.getLng())
                .dailyMenus(dailyMenuDtos)
                .build();
    }


}
