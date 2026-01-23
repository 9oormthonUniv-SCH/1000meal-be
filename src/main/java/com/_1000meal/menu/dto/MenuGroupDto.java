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

    public static MenuGroupDto from(MenuGroup group) {
        return MenuGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .stock(group.getStock() != null ? group.getStock().getStock() : 0)
                .capacity(group.getStock() != null ? group.getStock().getCapacity() : 100)
                .menus(group.getMenus().stream().map(Menu::getName).toList())
                .build();
    }
}
