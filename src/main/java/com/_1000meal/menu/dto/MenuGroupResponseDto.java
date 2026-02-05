package com._1000meal.menu.dto;

import com._1000meal.menu.domain.MenuGroup;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenuGroupResponseDto {
    private Long id;
    private String name;
    private Integer sortOrder;
    private boolean isDefault;
    @Builder.Default
    private List<MenuResponseDto> menus = List.of();

    public static MenuGroupResponseDto from(MenuGroup group, List<MenuResponseDto> menus) {
        return MenuGroupResponseDto.builder()
                .id(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .isDefault(group.isDefault())
                .menus(menus != null ? menus : List.of())
                .build();
    }
}
