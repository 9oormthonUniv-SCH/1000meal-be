package com._1000meal.favoriteMenu.controller;

import com._1000meal.favoriteMenu.dto.FavoriteMenuGroupedResponse;
import com._1000meal.global.error.code.SuccessCode;
import com._1000meal.favoriteMenu.service.FavoriteMenuService;
import com._1000meal.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteMenuController {

    private final FavoriteMenuService favoriteService;

    @PostMapping("/{storeId}")
    public ApiResponse<Void> createfavoriteMenu(
            @PathVariable Long storeId,
            @RequestBody List<String> names) {

        favoriteService.createGroupAndReplaceFavorites(storeId,names);

        return ApiResponse.success(null, SuccessCode.OK);
    }

    @GetMapping("/store/{storeId}")
    public ApiResponse<FavoriteMenuGroupedResponse> listAllFavoritesGrouped(@PathVariable Long storeId) {

        FavoriteMenuGroupedResponse resp = favoriteService.getAllFavoritesGrouped(storeId);

        return ApiResponse.success(resp, SuccessCode.OK);
    }

    @GetMapping("/group/{groupId}")
    public ApiResponse<FavoriteMenuGroupedResponse> listFavoritesGrouped(
            @PathVariable Long groupId) {

        FavoriteMenuGroupedResponse resp = favoriteService.getFavoritesGroupedByGroup(groupId);

        return ApiResponse.success(resp, SuccessCode.OK);
    }

    @PutMapping("/{groupId}")
    public ApiResponse<Void> updateGroupItems(
            @PathVariable Long groupId,
            @RequestBody List<String> names) {

        favoriteService.replaceFavoritesInGroup(groupId, names);

        return ApiResponse.success(null, SuccessCode.OK);
    }

    @DeleteMapping("/{storeId}/groups")
    public ApiResponse<Void> deleteGroups(
            @PathVariable Long storeId,
            @RequestBody List<Long> groupIds) {

         favoriteService.deleteGroups(storeId, groupIds);

        return ApiResponse.success(null, SuccessCode.OK);
    }
}
