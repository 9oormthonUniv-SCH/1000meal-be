package com._1000meal.menu.dto;

import com._1000meal.menu.domain.MenuGroupDayCapacity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
public class MenuGroupDayCapacityAdminResponse {

    @Schema(description = "매장 ID")
    private Long storeId;

    @Schema(description = "메뉴 그룹 ID")
    private Long groupId;

    @Schema(description = "요일별 기본 수량")
    private List<Entry> entries;

    @Getter
    @Builder
    public static class Entry {
        private DayOfWeek dayOfWeek;
        private Integer capacity;

        public static Entry from(MenuGroupDayCapacity row) {
            return Entry.builder()
                    .dayOfWeek(row.getDayOfWeek())
                    .capacity(row.getCapacity())
                    .build();
        }
    }

    public static MenuGroupDayCapacityAdminResponse of(Long storeId, Long groupId, List<MenuGroupDayCapacity> rows) {
        List<Entry> entries = rows.stream()
                .map(Entry::from)
                .sorted(Comparator.comparing(e -> e.getDayOfWeek().getValue()))
                .toList();
        return MenuGroupDayCapacityAdminResponse.builder()
                .storeId(storeId)
                .groupId(groupId)
                .entries(entries)
                .build();
    }
}
