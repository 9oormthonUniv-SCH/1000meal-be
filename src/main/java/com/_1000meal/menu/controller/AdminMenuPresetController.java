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

@Tag(
        name = "관리자 - 자주 쓰는 메뉴",
        description = "관리자 전용 자주 쓰는 메뉴(프리셋) API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminMenuPresetController {

    private final MenuPresetService menuPresetService;

    @Operation(
            summary = "자주 쓰는 메뉴 목록 조회",
            description = """
                    매장별 자주 쓰는 메뉴(프리셋) 목록을 조회합니다.

                    - 목록 항목은 preview(메뉴명을 콤마로 연결한 문자열)와 생성/수정일을 포함합니다.
                    - 관리자 권한(ROLE_ADMIN)만 호출 가능합니다.
                    - storeId는 현재 로그인한 관리자의 매장과 일치해야 합니다.
                    """
    )
    @GetMapping("/stores/{storeId}/menu-presets")
    public ApiResponse<List<MenuPresetSummaryResponse>> list(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId
    ) {
        return ApiResponse.ok(menuPresetService.list(storeId));
    }

    @Operation(
            summary = "자주 쓰는 메뉴 생성",
            description = """
                    입력된 메뉴 목록(menus)만으로 자주 쓰는 메뉴(프리셋)를 생성합니다.

                    - menus는 입력 순서를 유지합니다.
                    - 서버에서 각 메뉴명을 trim 처리 후, 공백/빈 문자열은 제거합니다.
                    - trim/제거 후 menus가 비어 있으면 MENU_EMPTY 오류가 발생합니다.
                    - 관리자 권한(ROLE_ADMIN)만 호출 가능합니다.
                    - storeId는 현재 로그인한 관리자의 매장과 일치해야 합니다.
                    """
    )
    @PostMapping("/stores/{storeId}/menu-presets")
    public ApiResponse<MenuPresetDetailResponse> create(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Valid @RequestBody MenuPresetCreateRequest request
    ) {
        MenuPresetDetailResponse response = menuPresetService.create(storeId, request);
        return ApiResponse.success(response, SuccessCode.CREATED);
    }

    @Operation(
            summary = "자주 쓰는 메뉴 상세 조회",
            description = """
                    자주 쓰는 메뉴(프리셋) 상세 정보를 조회합니다.

                    - menus(메뉴명 목록)과 preview(메뉴명을 콤마로 연결한 문자열)를 반환합니다.
                    - 관리자 권한(ROLE_ADMIN)만 호출 가능합니다.
                    - storeId는 현재 로그인한 관리자의 매장과 일치해야 합니다.
                    """
    )
    @GetMapping("/stores/{storeId}/menu-presets/{presetId}")
    public ApiResponse<MenuPresetDetailResponse> get(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "자주 쓰는 메뉴 ID", example = "101")
            @PathVariable Long presetId
    ) {
        return ApiResponse.ok(menuPresetService.get(storeId, presetId));
    }

    @Operation(
            summary = "자주 쓰는 메뉴 삭제",
            description = """
                    자주 쓰는 메뉴(프리셋)를 삭제합니다.

                    - 관리자 권한(ROLE_ADMIN)만 호출 가능합니다.
                    - storeId는 현재 로그인한 관리자의 매장과 일치해야 합니다.
                    """
    )
    @DeleteMapping("/stores/{storeId}/menu-presets/{presetId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "자주 쓰는 메뉴 ID", example = "101")
            @PathVariable Long presetId
    ) {
        menuPresetService.delete(storeId, presetId);
        return ApiResponse.success(null, SuccessCode.OK);
    }
}
