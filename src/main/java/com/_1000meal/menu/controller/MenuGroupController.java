package com._1000meal.menu.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.*;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.service.MenuGroupService;
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
            summary = "그룹 재고 차감",
            description = """
                    메뉴 그룹의 재고를 차감합니다.

                    - 차감 단위는 enum(DeductionUnit)으로 지정합니다.
                    - 재고가 부족하면 INSUFFICIENT_STOCK 오류가 발생합니다.
                    - 재고가 10개 이하로 떨어지면 즐겨찾기 사용자에게 품절 임박 알림이 발송됩니다.
                    """
    )
    @PatchMapping("/groups/{groupId}/deduct")
    public ApiResponse<MenuGroupStockResponse> deductGroupStock(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @Parameter(
                    description = "차감 단위 (SINGLE=1, MULTI_FIVE=5, MULTI_TEN=10)",
                    example = "SINGLE"
            )
            @RequestParam DeductionUnit deductionUnit
    ) {
        return ApiResponse.ok(menuGroupService.deductStock(groupId, deductionUnit));
    }

    @Operation(
            summary = "그룹 재고 직접 수정",
            description = "메뉴 그룹의 재고를 특정 값으로 설정합니다."
    )
    @PostMapping("/groups/{groupId}/stock")
    public ApiResponse<MenuGroupStockResponse> updateGroupStock(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @Valid @RequestBody StockUpdateRequest request
    ) {
        return ApiResponse.success(
                menuGroupService.updateStock(groupId, request.getStock()),
                SuccessCode.UPDATED
        );
    }

    @Operation(
            summary = "그룹 메뉴 등록/교체",
            description = """
                    메뉴 그룹의 특정 날짜 메뉴를 등록하거나 기존 메뉴를 교체합니다.

                    - (groupId, date) 조합으로 upsert 동작합니다.
                    - 해당 조합이 없으면 새로 생성, 있으면 메뉴를 교체합니다.
                    - 메뉴명은 trim 처리되며, 빈 값/중복 값은 제거됩니다.
                    """
    )
    @PostMapping("/groups/{groupId}/menus")
    public ApiResponse<GroupDailyMenuResponse> updateMenusInGroup(
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @Parameter(description = "날짜 (YYYY-MM-DD)", example = "2026-01-23")
            @RequestParam LocalDate date,

            @Valid @RequestBody MenuUpdateRequest request
    ) {
        return ApiResponse.success(
                menuGroupService.updateMenusInGroup(groupId, date, request),
                SuccessCode.OK
        );
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
