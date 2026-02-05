package com._1000meal.menu.service;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.DefaultGroupMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.dto.DefaultMenuResponse;
import com._1000meal.menu.repository.DefaultGroupMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultGroupMenuService {

    private final DefaultGroupMenuRepository defaultGroupMenuRepository;
    private final MenuGroupRepository menuGroupRepository;

    @Transactional
    public DefaultMenuResponse pinDefaultMenu(Long storeId, Long groupId, String menuName) {
        String trimmed = menuName == null ? "" : menuName.trim();
        if (trimmed.isEmpty()) {
            throw new CustomException(MenuErrorCode.INVALID_MENU_NAME);
        }

        MenuGroup menuGroup = getAuthorizedGroupForStore(storeId, groupId);

        DefaultGroupMenu existing = defaultGroupMenuRepository.findOpenRule(groupId, trimmed)
                .orElse(null);
        if (existing != null) {
            return DefaultMenuResponse.from(existing, LocalDate.now(ZoneId.of("Asia/Seoul")));
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        DefaultGroupMenu rule = DefaultGroupMenu.builder()
                .store(menuGroup.getStore())
                .menuGroup(menuGroup)
                .menuName(trimmed)
                .startDate(today.plusDays(1))
                .endDate(null)
                .createdByAccountId(null)
                .build();

        DefaultGroupMenu saved = defaultGroupMenuRepository.save(rule);
        return DefaultMenuResponse.from(saved, today);
    }

    @Transactional
    public DefaultMenuResponse unpinDefaultMenu(Long storeId, Long groupId, String menuName) {
        String trimmed = menuName == null ? "" : menuName.trim();
        if (trimmed.isEmpty()) {
            throw new CustomException(MenuErrorCode.INVALID_MENU_NAME);
        }

        getAuthorizedGroupForStore(storeId, groupId);

        DefaultGroupMenu rule = defaultGroupMenuRepository.findOpenRule(groupId, trimmed)
                .orElseThrow(() -> new CustomException(MenuErrorCode.DEFAULT_MENU_NOT_FOUND));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        rule.close(today);

        return DefaultMenuResponse.from(rule, today);
    }

    @Transactional(readOnly = true)
    public List<DefaultMenuResponse> getDefaultMenus(Long storeId, Long groupId) {
        getAuthorizedGroupForStore(storeId, groupId);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return defaultGroupMenuRepository.findByMenuGroupIdOrderByStartDateDescIdDesc(groupId).stream()
                .map(rule -> DefaultMenuResponse.from(rule, today))
                .toList();
    }

    private MenuGroup getAuthorizedGroupForStore(Long storeId, Long groupId) {
        MenuGroup menuGroup = menuGroupRepository.findByIdWithStore(groupId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        Long groupStoreId = menuGroup.getStore() != null ? menuGroup.getStore().getId() : null;
        if (groupStoreId == null || !groupStoreId.equals(storeId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        return menuGroup;
    }
}
