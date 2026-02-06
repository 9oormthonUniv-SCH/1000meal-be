package com._1000meal.menu.dto;

import com._1000meal.menu.domain.Menu;
import com._1000meal.menu.domain.MenuGroup;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenuGroupDto {
    private Long id;
    private String name;
    private Integer sortOrder;
    private Integer stock;
    private Integer capacity;
    private List<String> menus;
    private List<MenuItemDto> menuItems;

    public static MenuGroupDto from(MenuGroup group) {
        List<String> menus = group.getMenus().stream().map(Menu::getName).toList();
        List<MenuItemDto> menuItems = menus.stream()
                .map(name -> new MenuItemDto(name, false))
                .toList();

        return MenuGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(menus)
                .menuItems(menuItems)
                .build();
    }

    public static MenuGroupDto from(MenuGroup group, List<String> menus) {
        List<String> normalizedMenus = (menus != null ? menus : List.<String>of());

        List<MenuItemDto> menuItems = normalizedMenus.stream()
                .map(name -> new MenuItemDto(name, false))
                .toList();

        return MenuGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(normalizedMenus)
                .menuItems(menuItems)
                .build();
    }

    public static MenuGroupDto from(MenuGroup group, List<String> menus, List<MenuItemDto> menuItems) {
        List<String> normalizedMenus = menus != null ? menus : List.of();
        List<MenuItemDto> normalizedItems = menuItems != null ? menuItems : List.of();

        return MenuGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(normalizedMenus)
                .menuItems(normalizedItems)
                .build();
    }
}
