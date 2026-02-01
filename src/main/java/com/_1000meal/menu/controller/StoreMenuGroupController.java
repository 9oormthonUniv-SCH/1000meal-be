package com._1000meal.menu.controller;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.GroupDailyMenuResponse;
import com._1000meal.menu.dto.MenuGroupStockResponse;
import com._1000meal.menu.dto.MenuUpdateRequest;
import com._1000meal.menu.dto.StockUpdateRequest;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.service.MenuGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Menu Group", description = "매장별 메뉴 그룹 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreMenuGroupController {

    private final MenuGroupService menuGroupService;
    private final CurrentAccountProvider currentAccountProvider;

    @Operation(
            summary = "매장 기준 그룹 메뉴 등록/교체",
            description = """
                    storeId와 groupId를 함께 받아 특정 매장의 메뉴 그룹에 메뉴를 등록/교체합니다.

                    - storeId는 로그인 계정의 storeId와 반드시 일치해야 합니다.
                    - 그룹 조회는 (groupId, storeId) 조합으로 안전하게 처리됩니다.
                    """
    )
    @PostMapping("/{storeId}/menus/daily/groups/{groupId}/menus")
    public ApiResponse<GroupDailyMenuResponse> updateMenusInGroupForStore(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @Parameter(description = "날짜 (YYYY-MM-DD)", example = "2026-01-23")
            @RequestParam LocalDate date,

            @Valid @RequestBody MenuUpdateRequest request
    ) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return ApiResponse.ok(menuGroupService.updateMenusInGroupForStore(storeId, groupId, date, request));
    }

    @Operation(
            summary = "매장 기준 그룹 재고 차감",
            description = """
                    storeId와 groupId를 함께 받아 특정 매장의 그룹 재고를 차감합니다.

                    - storeId는 로그인 계정의 storeId와 반드시 일치해야 합니다.
                    - 그룹의 storeId도 path storeId와 일치해야 합니다.
                    """
    )
    @PostMapping("/{storeId}/menus/daily/groups/{groupId}/deduct")
    public ApiResponse<MenuGroupStockResponse> deductGroupStockForStore(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @Parameter(
                    description = "차감 단위 (SINGLE=1, MULTI_FIVE=5, MULTI_TEN=10)",
                    example = "SINGLE"
            )
            @RequestParam DeductionUnit deductionUnit
    ) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return ApiResponse.ok(menuGroupService.deductStockForStore(storeId, groupId, deductionUnit));
    }

    @Operation(
            summary = "매장 기준 그룹 재고 직접 수정",
            description = """
                    storeId와 groupId를 함께 받아 특정 매장의 그룹 재고를 설정합니다.

                    - storeId는 로그인 계정의 storeId와 반드시 일치해야 합니다.
                    - 그룹의 storeId도 path storeId와 일치해야 합니다.
                    """
    )
    @PostMapping("/{storeId}/menus/daily/groups/{groupId}/stock")
    public ApiResponse<MenuGroupStockResponse> updateGroupStockForStore(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @Valid @RequestBody StockUpdateRequest request
    ) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return ApiResponse.ok(menuGroupService.updateStockForStore(storeId, groupId, request.getStock()));
    }
}
