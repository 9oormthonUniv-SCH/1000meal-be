package com._1000meal.menu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DefaultMenuActivateResponse {
    private DefaultMenuResponse defaultMenu;
    private DefaultMenuMaterializeResult materialized;
}
