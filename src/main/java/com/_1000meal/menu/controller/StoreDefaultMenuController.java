package com._1000meal.menu.controller;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.DefaultMenuActivateRequest;
import com._1000meal.menu.dto.DefaultMenuActivateResponse;
import com._1000meal.menu.dto.DefaultMenuRequest;
import com._1000meal.menu.dto.DefaultMenuResponse;
import com._1000meal.menu.service.DefaultGroupMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Default Menu", description = "매장 기본(핀) 메뉴 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreDefaultMenuController {

    private final DefaultGroupMenuService defaultGroupMenuService;
    private final CurrentAccountProvider currentAccountProvider;

    @Operation(
            summary = "기본(핀) 메뉴 설정",
            description = """
                    특정 메뉴 그룹에 기본(핀) 메뉴를 설정합니다.

                    - storeId는 로그인 계정의 storeId와 반드시 일치해야 합니다.
                    - 그룹의 storeId도 path storeId와 일치해야 합니다.
                    - 시작일은 자동으로 '다음날'로 설정됩니다.
                    """
    )
    @PostMapping("/{storeId}/menu-groups/{groupId}/default-menus")
    public ApiResponse<DefaultMenuResponse> createDefaultMenu(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @Valid @RequestBody DefaultMenuRequest request
    ) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return ApiResponse.ok(defaultGroupMenuService.pinDefaultMenu(
                storeId,
                groupId,
                request.getMenuName(),
                request.getStartDate(),
                request.getEndDate()
        ));
    }

    @Operation(
            summary = "기본(핀) 메뉴 해제",
            description = """
                    기본(핀) 메뉴를 해제합니다.

                    - storeId는 로그인 계정의 storeId와 반드시 일치해야 합니다.
                    - 그룹의 storeId도 path storeId와 일치해야 합니다.
                    - 해제일은 오늘로 설정됩니다.
                    """
    )
    @DeleteMapping("/{storeId}/menu-groups/{groupId}/default-menus")
    public ApiResponse<DefaultMenuResponse> deleteDefaultMenu(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @RequestParam String menuName
    ) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return ApiResponse.ok(defaultGroupMenuService.unpinDefaultMenu(storeId, groupId, menuName));
    }

    @Operation(
            summary = "기본(핀) 메뉴 조회",
            description = """
                    특정 메뉴 그룹의 기본(핀) 메뉴 규칙을 조회합니다.

                    - storeId는 로그인 계정의 storeId와 반드시 일치해야 합니다.
                    - 그룹의 storeId도 path storeId와 일치해야 합니다.
                    """
    )
    @GetMapping("/{storeId}/menu-groups/{groupId}/default-menus")
    public ApiResponse<List<DefaultMenuResponse>> getDefaultMenus(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return ApiResponse.ok(defaultGroupMenuService.getDefaultMenus(storeId, groupId));
    }

    @Operation(
            summary = "기본(핀) 메뉴 활성화 및 일간 메뉴 전개",
            description = """
                    기본(핀) 메뉴를 활성화하고, 오늘 날짜의 일간 메뉴로 전개합니다.

                    - storeId는 로그인 계정의 storeId와 반드시 일치해야 합니다.
                    - 그룹의 storeId도 path storeId와 일치해야 합니다.
                    - 기본 동작은 '오늘 하루' 전개입니다.
                    """
    )
    @PatchMapping("/{storeId}/menu-groups/{groupId}/default-menus/{defaultMenuId}/activate")
    public ApiResponse<DefaultMenuActivateResponse> activateDefaultMenu(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,

            @Parameter(description = "기본 메뉴 ID", example = "1")
            @PathVariable Long defaultMenuId,

            @RequestBody(required = false) DefaultMenuActivateRequest request
    ) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return ApiResponse.ok(defaultGroupMenuService.activateDefaultMenu(storeId, groupId, defaultMenuId, request));
    }
}
