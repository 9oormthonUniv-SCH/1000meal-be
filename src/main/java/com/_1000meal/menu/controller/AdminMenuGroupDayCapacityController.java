package com._1000meal.menu.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.MenuGroupDayCapacityAdminResponse;
import com._1000meal.menu.dto.MenuGroupDayCapacityUpdateRequest;
import com._1000meal.menu.service.MenuGroupDayCapacityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Menu Group Day Capacity", description = "관리자 전용 메뉴 그룹 요일별 기본 재고(용량) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminMenuGroupDayCapacityController {

    private final MenuGroupDayCapacityService menuGroupDayCapacityService;

    @Operation(
            summary = "메뉴 그룹 요일별 기본 재고 수정",
            description = """
                    특정 매장·메뉴 그룹의 요일별 기본 수량(menu_group_capacity_by_day)을 수정합니다.

                    - 자정 재고 리셋 시 이 값이 사용됩니다.
                    - 요청에 포함된 요일만 갱신되며, 해당 요일 행이 없으면 생성합니다.
                    - 관리자 권한(ROLE_ADMIN)만 접근 가능합니다.
                    """
    )
    @PatchMapping("/stores/{storeId}/menus/daily/groups/{groupId}/capacity-by-day")
    public ApiResponse<MenuGroupDayCapacityAdminResponse> updateCapacities(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "메뉴 그룹 ID", example = "10")
            @PathVariable Long groupId,
            @Valid @RequestBody MenuGroupDayCapacityUpdateRequest request
    ) {
        MenuGroupDayCapacityAdminResponse response =
                menuGroupDayCapacityService.updateCapacitiesForAdmin(storeId, groupId, request);
        return ApiResponse.success(response, SuccessCode.UPDATED);
    }
}
