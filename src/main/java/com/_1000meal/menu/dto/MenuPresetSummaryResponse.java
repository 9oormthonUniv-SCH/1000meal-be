package com._1000meal.menu.dto;

import com._1000meal.menu.domain.MenuPreset;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MenuPresetSummaryResponse {
    private Long id;
    private String preview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MenuPresetSummaryResponse from(MenuPreset preset) {
        return MenuPresetSummaryResponse.builder()
                .id(preset.getId())
                .preview(joinPreview(preset.getMenus()))
                .createdAt(preset.getCreatedAt())
                .updatedAt(preset.getUpdatedAt())
                .build();
    }

    private static String joinPreview(List<String> menus) {
        if (menus == null || menus.isEmpty()) {
            return "";
        }
        return String.join(", ", menus);
    }
}
