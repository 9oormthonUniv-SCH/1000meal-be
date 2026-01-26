package com._1000meal.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuGroupCreateRequest {

    // ✅ 선택값: 그룹명은 없어도 됨
    // (서버에서 기본값 생성 권장)
    private String name;

    private Integer sortOrder;

    @Min(value = 1, message = "최대 재고량은 1 이상이어야 합니다.")
    private Integer capacity;

    // ✅ 필수값: 메뉴는 반드시 1개 이상
    @NotEmpty(message = "메뉴는 최소 1개 이상 필요합니다.")
    @Size(min = 1, message = "메뉴는 최소 1개 이상 필요합니다.")
    private List<
            @NotBlank(message = "메뉴명은 비어 있을 수 없습니다.")
                    String
            > menus;

    /* =======================
       기본값 처리 헬퍼 메서드
       ======================= */

    public int getCapacityOrDefault() {
        return capacity != null ? capacity : 100;
    }

    public int getSortOrderOrDefault() {
        return sortOrder != null ? sortOrder : 0;
    }

    public String getNameOrDefault() {
        return (name == null || name.isBlank()) ? "기본 메뉴" : name.trim();
    }
}
