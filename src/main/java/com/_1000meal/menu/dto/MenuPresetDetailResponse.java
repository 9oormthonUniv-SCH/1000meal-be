package com._1000meal.menu.dto;

import com._1000meal.menu.domain.MenuPreset;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MenuPresetDetailResponse {
    private Long id;
    private Long storeId;
    private Long groupId;
    private List<String> menus;
    private String preview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MenuPresetDetailResponse from(MenuPreset preset) {
        Long storeId = preset.getStore() != null ? preset.getStore().getId() : null;
        List<String> menus = preset.getMenus() != null ? List.copyOf(preset.getMenus()) : List.of();
        return MenuPresetDetailResponse.builder()
                .id(preset.getId())
                .storeId(storeId)
                .groupId(preset.getGroupId())
                .menus(menus)
                .preview(String.join(", ", menus))
                .createdAt(preset.getCreatedAt())
                .updatedAt(preset.getUpdatedAt())
                .build();
    }
}
