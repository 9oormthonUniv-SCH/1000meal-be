package com._1000meal.favorite.controller;

import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.favorite.domain.FavoriteStore;
import com._1000meal.favorite.dto.FavoriteStoreItemResponse;
import com._1000meal.favorite.dto.FavoriteStoreToggleResponse;
import com._1000meal.favorite.service.FavoriteStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/favorites")
public class FavoriteStoreController {

    private final FavoriteStoreService favoriteStoreService;

    @PostMapping("/stores/{storeId}")
    public FavoriteStoreToggleResponse add(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long storeId
    ) {
        Long accountId = principal.id(); // 또는 principal.getId()
        favoriteStoreService.addFavorite(accountId, storeId);
        return FavoriteStoreToggleResponse.of(storeId, true);
    }

    @DeleteMapping("/stores/{storeId}")
    public FavoriteStoreToggleResponse remove(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long storeId
    ) {
        Long accountId = principal.id(); // 또는 principal.getId()
        favoriteStoreService.removeFavorite(accountId, storeId);
        return FavoriteStoreToggleResponse.of(storeId, false);
    }

    @GetMapping("/stores")
    public List<FavoriteStoreItemResponse> myFavorites(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        Long accountId = principal.id(); // 또는 principal.getId()

        return favoriteStoreService.getMyFavorites(accountId).stream()
                .map(FavoriteStore::getStore)
                .map(FavoriteStoreItemResponse::from)
                .toList();
    }
}