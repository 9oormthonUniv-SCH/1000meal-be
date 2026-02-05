package com._1000meal.menu.dto;

import com._1000meal.menu.domain.MenuGroup;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenuGroupResponse {
    private Long groupId;
    private String name;
    private Integer sortOrder;
    private Integer stock;
    private Integer capacity;
    private List<String> menus;

    public static MenuGroupResponse from(MenuGroup group) {
        return MenuGroupResponse.builder()
                .groupId(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(List.of())
                .build();
    }

    public static MenuGroupResponse from(MenuGroup group, List<String> menus) {
        return MenuGroupResponse.builder()
                .groupId(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(menus != null ? menus : List.of())
                .build();
    }
}
