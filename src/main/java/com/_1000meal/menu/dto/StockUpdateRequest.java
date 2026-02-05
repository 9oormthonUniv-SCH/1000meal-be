package com._1000meal.menu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StockUpdateRequest {
    @NotNull(message = "재고량은 필수입니다.")
    @Min(value = 0, message = "재고량은 0 이상이어야 합니다.")
    @Max(value = 100, message = "최대 재고량은 100입니다.")
    Integer stock;
}
