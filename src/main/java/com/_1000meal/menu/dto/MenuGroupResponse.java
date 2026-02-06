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
    private List<MenuItemDto> menuItems;

    public static MenuGroupResponse from(MenuGroup group) {
        return MenuGroupResponse.builder()
                .groupId(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(List.of())
                .menuItems(List.of())
                .build();
    }

    public static MenuGroupResponse from(MenuGroup group, List<String> menus) {
        List<String> safeMenus = menus != null ? List.copyOf(menus) : List.of();
        List<MenuItemDto> items = safeMenus.stream()
                .map((String name) -> new MenuItemDto(name, false))
                .toList();

        return MenuGroupResponse.builder()
                .groupId(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(safeMenus)
                .menuItems(items)
                .build();
    }

    public static MenuGroupResponse from(MenuGroup group, List<String> menus, List<MenuItemDto> menuItems) {
        return MenuGroupResponse.builder()
                .groupId(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(menus != null ? menus : List.of())
                .menuItems(menuItems != null ? menuItems : List.of())
                .build();
    }

    public static MenuGroupResponse from(MenuGroupDto dto) {
        return MenuGroupResponse.builder()
                .groupId(dto.getId())
                .name(dto.getName())
                .sortOrder(dto.getSortOrder())
                .stock(dto.getStock())
                .capacity(dto.getCapacity())
                .menus(dto.getMenus() != null ? dto.getMenus() : List.of())
                .menuItems(dto.getMenuItems() != null ? dto.getMenuItems() : List.of())
                .build();
    }
}
