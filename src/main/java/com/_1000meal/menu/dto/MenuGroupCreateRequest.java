package com._1000meal.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuGroupCreateRequest {

    @NotBlank(message = "그룹명은 필수입니다.")
    private String name;

    private Integer sortOrder;

    @Min(value = 1, message = "최대 재고량은 1 이상이어야 합니다.")
    private Integer capacity;

    public int getCapacityOrDefault() {
        return capacity != null ? capacity : 100;
    }

    public int getSortOrderOrDefault() {
        return sortOrder != null ? sortOrder : 0;
    }
}
