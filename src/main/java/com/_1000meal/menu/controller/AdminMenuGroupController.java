package com._1000meal.menu.controller;

import com._1000meal.global.response.ApiResponse;
import com._1000meal.menu.dto.MenuGroupAdminResponse;
import com._1000meal.menu.service.MenuGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin Menu Group", description = "관리자 전용 메뉴 그룹 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminMenuGroupController {

    private final MenuGroupService menuGroupService;

    @Operation(
            summary = "매장별 메뉴 그룹 조회 (관리자)",
            description = """
                    특정 매장의 메뉴 그룹 목록을 조회합니다.

                    - 관리자 권한(ROLE_ADMIN)만 접근 가능합니다.
                    """
    )
    @GetMapping("/stores/{storeId}/menu-groups")
    public ApiResponse<List<MenuGroupAdminResponse>> getMenuGroupsForAdmin(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId
    ) {
        return ApiResponse.ok(menuGroupService.getMenuGroupsForAdmin(storeId));
    }
}
