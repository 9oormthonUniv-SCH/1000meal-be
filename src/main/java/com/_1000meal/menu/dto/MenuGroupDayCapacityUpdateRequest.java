package com._1000meal.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MenuGroupDayCapacityUpdateRequest {

    @Schema(description = "요일별 기본 수량(자정 재고 리셋 기준). 비어 있지 않아야 합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Valid
    private List<DayCapacityItem> capacities;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DayCapacityItem {

        @NotNull
        @Schema(example = "MONDAY/TUESDAY/WEDNESDAY/THURSDAY/FRIDAY")
        private DayOfWeek dayOfWeek;

        @NotNull
        @Min(0)
        @Schema(example = "80")
        private Integer capacity;
    }
}
