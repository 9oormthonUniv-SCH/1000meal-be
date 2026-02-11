package com._1000meal.menu.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.DailyMenuWithGroupsDto;
import com._1000meal.menu.dto.MenuGroupCreateRequest;
import com._1000meal.menu.dto.MenuGroupDto;
import com._1000meal.menu.dto.WeeklyMenuWithGroupsResponse;
import com._1000meal.menu.service.MenuGroupService;
import com._1000meal.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Menu Group", description = "메뉴 그룹 및 그룹별 재고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menus/daily")
public class MenuGroupController {

    private final MenuGroupService menuGroupService;
    private final MenuService menuService;

    @Operation(
            summary = "일간 메뉴 그룹 조회",
            description = """
                    특정 날짜의 메뉴 그룹 목록과 각 그룹의 재고를 조회합니다.

                    - 각 그룹별로 메뉴 목록과 재고가 반환됩니다.
                    - totalStock은 모든 그룹의 재고 합계입니다.
                    """
    )
    @GetMapping("/{storeId}/groups")
    public ApiResponse<DailyMenuWithGroupsDto> getMenuGroups(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "조회 날짜 (YYYY-MM-DD)", example = "2026-01-23")
            @RequestParam LocalDate date
    ) {
        return ApiResponse.ok(menuGroupService.getMenuGroups(storeId, date));
    }

    @Operation(
            summary = "주간 메뉴 조회",
            description = """
                    기준 날짜가 포함된 주간(월~일) 메뉴를 조회합니다.

                    - 주간 메뉴가 없으면 빈 스켈레톤 구조를 반환합니다.
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

    @Operation(
            summary = "메뉴 그룹 생성",
            description = """
                    매장에 새로운 메뉴 그룹을 생성합니다.

                    - 그룹명, 정렬 순서, 최대 재고량을 지정할 수 있습니다.
                    - 기본 그룹은 이 API로 생성하지 않습니다 (DB에 이미 존재).
                    """
    )
    @PostMapping("/{storeId}/groups")
    public ApiResponse<MenuGroupDto> createMenuGroup(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Valid @RequestBody MenuGroupCreateRequest request
    ) {
        return ApiResponse.success(
                menuGroupService.createMenuGroup(storeId, request),
                SuccessCode.CREATED
        );
    }

    @Operation(
            summary = "메뉴 그룹 삭제",
            description = "메뉴 그룹을 삭제합니다. 그룹에 속한 메뉴도 함께 삭제됩니다."
    )
    @DeleteMapping("/groups/{groupId}")
    public ApiResponse<Void> deleteMenuGroup(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        menuGroupService.deleteMenuGroup(groupId);
        return ApiResponse.success(null, SuccessCode.OK);
    }
}
