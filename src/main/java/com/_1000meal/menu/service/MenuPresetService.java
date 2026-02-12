package com._1000meal.menu.service;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.MenuPreset;
import com._1000meal.menu.dto.MenuPresetCreateRequest;
import com._1000meal.menu.dto.MenuPresetDetailResponse;
import com._1000meal.menu.dto.MenuPresetSummaryResponse;
import com._1000meal.menu.repository.MenuPresetRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuPresetService {

    private final MenuPresetRepository menuPresetRepository;
    private final StoreRepository storeRepository;
    private final CurrentAccountProvider currentAccountProvider;

    @Transactional
    public MenuPresetDetailResponse create(Long storeId, MenuPresetCreateRequest request) {
        validateStoreAccess(storeId);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        List<String> cleaned = cleanMenus(request.getMenus());
        if (cleaned.isEmpty()) {
            throw new CustomException(MenuErrorCode.MENU_EMPTY);
        }

        MenuPreset preset = MenuPreset.builder()
                .store(store)
                .createdByAccountId(currentAccountProvider.getCurrentAccountId())
                .menus(cleaned)
                .build();

        MenuPreset saved = menuPresetRepository.save(preset);
        return MenuPresetDetailResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuPresetSummaryResponse> list(Long storeId) {
        validateStoreAccess(storeId);
        storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        List<MenuPreset> raw = menuPresetRepository.findByStoreIdWithMenus(storeId);
        Map<Long, MenuPreset> unique = new LinkedHashMap<>();
        for (MenuPreset preset : raw) {
            unique.putIfAbsent(preset.getId(), preset);
        }

        return unique.values().stream()
                .map(MenuPresetSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuPresetDetailResponse get(Long storeId, Long presetId) {
        validateStoreAccess(storeId);
        MenuPreset preset = menuPresetRepository.findByIdAndStoreIdWithMenus(presetId, storeId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_PRESET_NOT_FOUND));
        return MenuPresetDetailResponse.from(preset);
    }

    @Transactional
    public void delete(Long storeId, Long presetId) {
        validateStoreAccess(storeId);
        MenuPreset preset = menuPresetRepository.findByIdAndStoreId(presetId, storeId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_PRESET_NOT_FOUND));
        menuPresetRepository.delete(preset);
    }

    private void validateStoreAccess(Long storeId) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }
    }

    private List<String> cleanMenus(List<String> menus) {
        if (menus == null) {
            return List.of();
        }
        return menus.stream()
                .map(menu -> menu == null ? "" : menu.trim())
                .filter(menu -> !menu.isEmpty())
                .toList();
    }
}
