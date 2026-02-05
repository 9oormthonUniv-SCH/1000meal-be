package com._1000meal.menu.dto;

import com._1000meal.menu.domain.GroupDailyMenu;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class GroupDailyMenuResponse {
    private Long id;
    private Long groupId;
    private String groupName;
    private LocalDate date;
    private List<String> menus;

    public static GroupDailyMenuResponse from(GroupDailyMenu groupDailyMenu) {
        return GroupDailyMenuResponse.builder()
                .id(groupDailyMenu.getId())
                .groupId(groupDailyMenu.getMenuGroup().getId())
                .groupName(groupDailyMenu.getMenuGroup().getName())
                .date(groupDailyMenu.getDate())
                .menus(List.copyOf(groupDailyMenu.getMenuNames()))
                .build();
    }
}
