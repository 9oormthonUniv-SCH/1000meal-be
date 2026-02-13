package com._1000meal.menu.service;

import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuPreset;
import com._1000meal.menu.dto.MenuPresetCreateRequest;
import com._1000meal.menu.dto.MenuPresetDetailResponse;
import com._1000meal.menu.dto.MenuPresetSummaryResponse;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.menu.repository.MenuPresetRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuPresetService {

    private final MenuPresetRepository menuPresetRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final StoreRepository storeRepository;
    private final CurrentAccountProvider currentAccountProvider;

    @Transactional
    public MenuPresetDetailResponse create(Long storeId, Long groupId, MenuPresetCreateRequest request) {
        validateStoreAccess(storeId);
        validateGroupScope(storeId, groupId);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        List<String> cleaned = cleanMenus(request.getMenus());
        if (cleaned.isEmpty()) {
            throw new CustomException(MenuErrorCode.MENU_EMPTY);
        }

        MenuPreset preset = MenuPreset.builder()
                .store(store)
                .groupId(groupId)
                .createdByAccountId(currentAccountProvider.getCurrentAccountId())
                .menus(cleaned)
                .build();

        MenuPreset saved = menuPresetRepository.save(preset);
        return MenuPresetDetailResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuPresetSummaryResponse> list(Long storeId, Long groupId) {
        validateStoreAccess(storeId);
        validateGroupScope(storeId, groupId);
        storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        List<MenuPreset> presets = menuPresetRepository.findAllByStoreIdAndGroupIdWithMenus(storeId, groupId);
        if (presets.isEmpty()) {
            throw new CustomException(MenuErrorCode.MENU_PRESET_EMPTY);
        }

        return presets.stream()
                .map(MenuPresetSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuPresetDetailResponse get(Long storeId, Long groupId, Long presetId) {
        validateStoreAccess(storeId);
        validateGroupScope(storeId, groupId);
        MenuPreset preset = menuPresetRepository.findByIdAndStoreIdAndGroupIdWithMenus(presetId, storeId, groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_PRESET_NOT_FOUND));
        return MenuPresetDetailResponse.from(preset);
    }

    @Transactional
    public void delete(Long storeId, Long groupId, Long presetId) {
        validateStoreAccess(storeId);
        validateGroupScope(storeId, groupId);
        MenuPreset preset = menuPresetRepository.findByIdAndStoreIdAndGroupId(presetId, storeId, groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_PRESET_NOT_FOUND));
        menuPresetRepository.delete(preset);
    }

    private void validateStoreAccess(Long storeId) {
        Long accountStoreId = currentAccountProvider.getCurrentStoreId();
        if (!storeId.equals(accountStoreId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }
    }

    private void validateGroupScope(Long storeId, Long groupId) {
        MenuGroup menuGroup = menuGroupRepository.findByIdAndStoreId(groupId, storeId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));
        if (menuGroup.getStore() == null || !storeId.equals(menuGroup.getStore().getId())) {
            throw new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND);
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
