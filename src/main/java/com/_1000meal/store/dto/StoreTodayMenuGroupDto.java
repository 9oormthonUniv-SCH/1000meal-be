package com._1000meal.store.dto;

import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.dto.MenuResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreTodayMenuGroupDto {
    private Long id;
    private String name;
    private Integer sortOrder;
    private boolean isDefault;
    private Integer capacity;
    private Integer stock;
    @Builder.Default
    private List<MenuResponseDto> menus = List.of();

    public static StoreTodayMenuGroupDto from(MenuGroup group, List<MenuResponseDto> menus) {
        Integer stock = group.getStock() != null ? group.getStock().getStock() : 0;
        Integer capacity = group.getStock() != null ? group.getStock().getCapacity() : 100;
        return StoreTodayMenuGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .isDefault(group.isDefault())
                .capacity(capacity)
                .stock(stock)
                .menus(menus != null ? menus : List.of())
                .build();
    }
}
