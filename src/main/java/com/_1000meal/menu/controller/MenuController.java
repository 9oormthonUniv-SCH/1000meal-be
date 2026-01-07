package com._1000meal.menu.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.*;
import com._1000meal.menu.enums.DeductionUnit;
import com._1000meal.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Menu", description = "메뉴 및 재고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;

    // ================================
    // 일간 메뉴 생성 / 수정
    // ================================
    @Operation(
            summary = "일간 메뉴 등록/수정",
            description = """
                    특정 날짜의 일간 메뉴를 등록하거나 기존 메뉴를 교체합니다.
                    
                    - 메뉴 리스트가 비어있으면 기존 메뉴는 모두 삭제됩니다.
                    - 메뉴명은 trim 처리되며, 빈 값/중복 값은 제거됩니다.
                    """
    )
    @PostMapping("/daily/{storeId}")
    public ApiResponse<DailyMenuAddRequest> createDailyMenu(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Valid @RequestBody DailyMenuAddRequest request
    ) {
        menuService.addOrReplaceDailyMenu(storeId, request);
        return ApiResponse.success(request, SuccessCode.OK);
    }

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
    // 일간 메뉴 조회
    // ================================
    @Operation(
            summary = "일간 메뉴 조회",
            description = """
                    특정 날짜의 일간 메뉴를 조회합니다.
                    
                    - 메뉴가 존재하지 않으면 DAILY_MENU_NOT_FOUND 오류가 발생합니다.
                    """
    )
    @GetMapping("/daily/{storeId}")
    public ApiResponse<DailyMenuDto> getDailyMenu(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "조회 날짜 (YYYY-MM-DD)", example = "2026-01-06")
            @RequestParam LocalDate date
    ) {
        DailyMenuDto resp = menuService.getDailyMenu(storeId, date);
        return ApiResponse.success(resp, SuccessCode.OK);
    }

    // ================================
    // 재고 차감
    // ================================
    @Operation(
            summary = "메뉴 재고 차감",
            description = """
                    메뉴 재고를 차감합니다.
                    
                    - 차감 단위는 enum(DeductionUnit)으로 지정합니다.
                    - 재고가 부족하면 INSUFFICIENT_STOCK 오류가 발생합니다.
                    """
    )
    @PatchMapping("/daily/deduct/{menuId}")
    public ApiResponse<StockResponse> deductStock(
            @Parameter(description = "메뉴 ID", example = "10")
            @PathVariable Long menuId,

            @Parameter(
                    description = "차감 단위 (SINGLE=1, MULTI_FIVE=5, MULTI_TEN=10)",
                    example = "SINGLE"
            )
            @RequestParam DeductionUnit deductionUnit
    ) {
        StockResponse response =
                menuService.deductStock(menuId, deductionUnit.getValue());

        return ApiResponse.ok(response);
    }

    // ================================
    // 재고 직접 수정
    // ================================
    @Operation(
            summary = "메뉴 재고 수정",
            description = """
                    메뉴 재고를 특정 값으로 직접 수정합니다.
                    
                    - 관리자/매장 관리용 API
                    """
    )
    @PostMapping("/daily/stock/{menuId}")
    public ApiResponse<StockResponse> updateStock(
            @Parameter(description = "메뉴 ID", example = "10")
            @PathVariable Long menuId,

            @Valid @RequestBody StockUpdateRequest request
    ) {
        StockResponse response =
                menuService.operationStock(menuId, request.getStock());

        return ApiResponse.success(response, SuccessCode.UPDATED);
    }
}