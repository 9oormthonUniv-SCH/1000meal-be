package com._1000meal.menu.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.dto.WeeklyMenuWithGroupsResponse;
import com._1000meal.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Menu", description = "메뉴 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;

    // ================================
    // 주간 메뉴 조회
    // ================================
    @Operation(
            summary = "주간 메뉴 조회",
            description = """
                    기준 날짜가 포함된 주간(월~일) 메뉴를 조회합니다.

                    - 주간 메뉴가 없으면 빈 스켈레톤 구조를 반환합니다.
                    - 월~금(영업일) 기준으로 반환됩니다.
                    """
    )
    @GetMapping("/weekly/{storeId}")
    public ApiResponse<WeeklyMenuResponse> getWeeklyMenu(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "기준 날짜 (YYYY-MM-DD)", example = "2026-01-06")
            @RequestParam LocalDate date
    ) {
        WeeklyMenuResponse response = menuService.getWeeklyMenu(storeId, date);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    // ================================
    // 주간 메뉴 그룹 조회
    // ================================
    @Operation(
            summary = "주간 메뉴 그룹 조회",
            description = """
                    기준 날짜가 포함된 주간(월~금) 메뉴를 그룹 단위로 조회합니다.

                    - 주간 메뉴가 없으면 빈 스켈레톤 구조를 반환합니다.
                    - 재고는 MenuGroupStock 기준으로 반환됩니다.
                    - 월~금(영업일) 기준으로 반환됩니다.
                    """
    )
    @GetMapping("/weekly/{storeId}/groups")
    public ApiResponse<WeeklyMenuWithGroupsResponse> getWeeklyMenuWithGroups(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "기준 날짜 (YYYY-MM-DD)", example = "2026-01-06")
            @RequestParam LocalDate date
    ) {
        WeeklyMenuWithGroupsResponse response = menuService.getWeeklyMenuWithGroups(storeId, date);
        return ApiResponse.success(response, SuccessCode.OK);
    }
}
