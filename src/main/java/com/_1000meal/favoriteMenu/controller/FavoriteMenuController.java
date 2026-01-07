package com._1000meal.favoriteMenu.controller;

import com._1000meal.favoriteMenu.dto.FavoriteMenuGroupedResponse;
import com._1000meal.favoriteMenu.service.FavoriteMenuService;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Favorite Menu", description = "매장 즐겨찾기 메뉴 관리 API")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteMenuController {

    private final FavoriteMenuService favoriteService;

    @Operation(
            summary = "즐겨찾기 메뉴 그룹 생성",
            description = """
                    특정 매장(storeId)에 대해 즐겨찾기 메뉴 그룹을 새로 생성하고,
                    전달된 메뉴 이름 목록으로 그룹의 즐겨찾기 메뉴를 설정합니다.

                    메뉴 이름은 trim 처리되며, 빈 값 및 중복 값은 제거됩니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "즐겨찾기 그룹 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    @PostMapping("/{storeId}")
    public ApiResponse<Void> createFavoriteMenu(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(
                    description = "즐겨찾기 메뉴 이름 목록",
                    example = "[\"김치찌개\", \"된장찌개\"]"
            )
            @RequestBody List<String> names
    ) {
        favoriteService.createGroupAndReplaceFavorites(storeId, names);
        return ApiResponse.success(null, SuccessCode.OK);
    }

    @Operation(
            summary = "매장 전체 즐겨찾기 그룹 조회",
            description = "매장 ID에 해당하는 모든 즐겨찾기 그룹과 메뉴 목록을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    @GetMapping("/store/{storeId}")
    public ApiResponse<FavoriteMenuGroupedResponse> listAllFavoritesGrouped(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId
    ) {
        FavoriteMenuGroupedResponse resp = favoriteService.getAllFavoritesGrouped(storeId);
        return ApiResponse.success(resp, SuccessCode.OK);
    }

    @Operation(
            summary = "특정 즐겨찾기 그룹 조회",
            description = "그룹 ID에 해당하는 즐겨찾기 메뉴 목록을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "즐겨찾기 그룹을 찾을 수 없음")
    })
    @GetMapping("/group/{groupId}")
    public ApiResponse<FavoriteMenuGroupedResponse> listFavoritesGrouped(
            @Parameter(description = "즐겨찾기 그룹 ID", example = "10")
            @PathVariable Long groupId
    ) {
        FavoriteMenuGroupedResponse resp = favoriteService.getFavoritesGroupedByGroup(groupId);
        return ApiResponse.success(resp, SuccessCode.OK);
    }

    @Operation(
            summary = "즐겨찾기 그룹 메뉴 수정",
            description = """
                    특정 즐겨찾기 그룹의 메뉴 목록을 새로 교체합니다.
                    전달된 메뉴 목록이 비어있으면 기존 즐겨찾기는 모두 삭제됩니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "즐겨찾기 그룹 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "즐겨찾기 그룹을 찾을 수 없음")
    })
    @PutMapping("/{groupId}")
    public ApiResponse<Void> updateGroupItems(
            @Parameter(description = "즐겨찾기 그룹 ID", example = "10")
            @PathVariable Long groupId,
            @Parameter(
                    description = "새 즐겨찾기 메뉴 이름 목록",
                    example = "[\"돈까스\", \"제육볶음\"]"
            )
            @RequestBody List<String> names
    ) {
        favoriteService.replaceFavoritesInGroup(groupId, names);
        return ApiResponse.success(null, SuccessCode.OK);
    }

    @Operation(
            summary = "즐겨찾기 그룹 삭제",
            description = """
                    특정 매장에 속한 즐겨찾기 그룹들을 삭제합니다.
                    그룹에 포함된 즐겨찾기 메뉴가 먼저 삭제된 후 그룹이 삭제됩니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "즐겨찾기 그룹 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    @DeleteMapping("/{storeId}/groups")
    public ApiResponse<Void> deleteGroups(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable Long storeId,
            @Parameter(
                    description = "삭제할 즐겨찾기 그룹 ID 목록",
                    example = "[10, 20]"
            )
            @RequestBody List<Long> groupIds
    ) {
        favoriteService.deleteGroups(storeId, groupIds);
        return ApiResponse.success(null, SuccessCode.OK);
    }
}