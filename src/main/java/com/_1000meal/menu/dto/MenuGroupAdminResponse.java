package com._1000meal.menu.dto;

import com._1000meal.menu.domain.MenuGroup;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuGroupAdminResponse {
    private Long groupId;
    private Long storeId;
    private String name;
    private Integer sortOrder;
    private boolean isDefault;

    public static MenuGroupAdminResponse from(MenuGroup group) {
        Long storeId = group.getStore() != null ? group.getStore().getId() : null;
        return MenuGroupAdminResponse.builder()
                .groupId(group.getId())
                .storeId(storeId)
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .isDefault(group.isDefault())
                .build();
    }
}
