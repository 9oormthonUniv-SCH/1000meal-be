package com._1000meal.menu.controller;

import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.MenuPresetCreateRequest;
import com._1000meal.menu.dto.MenuPresetDetailResponse;
import com._1000meal.menu.dto.MenuPresetSummaryResponse;
import com._1000meal.menu.service.MenuPresetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Menu Preset", description = "관리자 전용 자주 쓰는 메뉴 프리셋 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminMenuPresetController {

    private final MenuPresetService menuPresetService;

    @Operation(
            summary = "자주 쓰는 메뉴 목록 조회",
            description = """
                    특정 매장의 특정 메뉴 그룹에 속한 자주 쓰는 메뉴 프리셋 목록을 조회합니다.
                    - storeId와 groupId 범위로 제한됩니다.
                    - 목록이 비어 있으면 MENU_PRESET_EMPTY(404)를 반환합니다.
                    """
    )
    @GetMapping("/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets")
    public ApiResponse<List<MenuPresetSummaryResponse>> list(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "메뉴 그룹 ID", example = "10")
            @PathVariable Long groupId
    ) {
        return ApiResponse.ok(menuPresetService.list(storeId, groupId));
    }

    @Operation(
            summary = "자주 쓰는 메뉴 생성",
            description = """
                    특정 매장의 특정 메뉴 그룹에 자주 쓰는 메뉴 프리셋을 생성합니다.
                    - menus 입력 순서를 유지합니다.
                    - trim/filter 이후 비어 있으면 MENU_EMPTY(404)를 반환합니다.
                    """
    )
    @PostMapping("/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets")
    public ApiResponse<MenuPresetDetailResponse> create(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "메뉴 그룹 ID", example = "10")
            @PathVariable Long groupId,
            @Valid @RequestBody MenuPresetCreateRequest request
    ) {
        MenuPresetDetailResponse response = menuPresetService.create(storeId, groupId, request);
        return ApiResponse.success(response, SuccessCode.OK);
    }

    @Operation(
            summary = "자주 쓰는 메뉴 상세 조회",
            description = """
                    특정 매장/메뉴 그룹 범위에서 프리셋 상세를 조회합니다.
                    - 범위 밖 presetId는 MENU_PRESET_NOT_FOUND(404)를 반환합니다.
                    """
    )
    @GetMapping("/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets/{presetId}")
    public ApiResponse<MenuPresetDetailResponse> get(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "메뉴 그룹 ID", example = "10")
            @PathVariable Long groupId,
            @Parameter(description = "자주 쓰는 메뉴 ID", example = "101")
            @PathVariable Long presetId
    ) {
        return ApiResponse.ok(menuPresetService.get(storeId, groupId, presetId));
    }

    @Operation(
            summary = "자주 쓰는 메뉴 삭제",
            description = """
                    특정 매장/메뉴 그룹 범위에서 프리셋을 삭제합니다.
                    - 범위 밖 presetId는 MENU_PRESET_NOT_FOUND(404)를 반환합니다.
                    """
    )
    @DeleteMapping("/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets/{presetId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "메뉴 그룹 ID", example = "10")
            @PathVariable Long groupId,
            @Parameter(description = "자주 쓰는 메뉴 ID", example = "101")
            @PathVariable Long presetId
    ) {
        menuPresetService.delete(storeId, groupId, presetId);
        return ApiResponse.success(null, SuccessCode.OK);
    }
}

