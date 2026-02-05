package com._1000meal.menu.service;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.auth.service.CurrentAccountProvider;
import com._1000meal.menu.domain.DefaultGroupMenu;
import com._1000meal.menu.domain.GroupDailyMenu;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.dto.DefaultMenuActivateRequest;
import com._1000meal.menu.dto.DefaultMenuActivateResponse;
import com._1000meal.menu.dto.DefaultMenuMaterializeResult;
import com._1000meal.menu.dto.DefaultMenuResponse;
import com._1000meal.menu.repository.DefaultGroupMenuRepository;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGroupMenuService {

    private final DefaultGroupMenuRepository defaultGroupMenuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final GroupDailyMenuRepository groupDailyMenuRepository;
    private final CurrentAccountProvider currentAccountProvider;

    @Transactional
    public DefaultMenuResponse pinDefaultMenu(Long storeId, Long groupId, String menuName, LocalDate startDate, LocalDate endDate) {
        String trimmed = menuName == null ? "" : menuName.trim();
        if (trimmed.isEmpty()) {
            throw new CustomException(MenuErrorCode.INVALID_MENU_NAME);
        }

        MenuGroup menuGroup = getAuthorizedGroupForStore(storeId, groupId);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate effectiveStart = startDate != null ? startDate : today;
        LocalDate effectiveEnd = endDate;

        DefaultGroupMenu existing = defaultGroupMenuRepository.findOpenRule(groupId, trimmed)
                .orElse(null);
        if (existing != null) {
            existing.setActiveRule(effectiveStart, effectiveEnd);
            if (!effectiveStart.isAfter(today)) {
                materializeIfAbsentForDate(menuGroup, today);
            }
            log.debug("[DEFAULT_MENU][CREATE] storeId={}, groupId={}, defaultMenuId={}, startDate={}, endDate={}, active={}",
                    storeId, groupId, existing.getId(), effectiveStart, effectiveEnd, existing.isActive());
            return DefaultMenuResponse.from(existing, today);
        }

        DefaultGroupMenu rule = DefaultGroupMenu.builder()
                .store(menuGroup.getStore())
                .menuGroup(menuGroup)
                .menuName(trimmed)
                .active(true)
                .startDate(effectiveStart)
                .endDate(effectiveEnd)
                .createdByAccountId(null)
                .build();

        DefaultGroupMenu saved = defaultGroupMenuRepository.save(rule);
        if (!effectiveStart.isAfter(today)) {
            materializeIfAbsentForDate(menuGroup, today);
        }
        log.debug("[DEFAULT_MENU][CREATE] storeId={}, groupId={}, defaultMenuId={}, startDate={}, endDate={}, active={}",
                storeId, groupId, saved.getId(), effectiveStart, effectiveEnd, saved.isActive());
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

    @Transactional
    public DefaultMenuActivateResponse activateDefaultMenu(
            Long storeId,
            Long groupId,
            Long defaultMenuId,
            DefaultMenuActivateRequest request
    ) {
        MenuGroup menuGroup = getAuthorizedGroupForStore(storeId, groupId);
        DefaultGroupMenu rule = defaultGroupMenuRepository.findById(defaultMenuId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.DEFAULT_MENU_NOT_FOUND));

        if (!rule.getMenuGroup().getId().equals(groupId) || !rule.getStore().getId().equals(storeId)) {
            throw new CustomException(StoreErrorCode.STORE_ACCESS_DENIED);
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate startDate = request != null ? request.getStartDate() : null;
        LocalDate endDate = request != null ? request.getEndDate() : null;

        LocalDate effectiveStart = (startDate != null) ? startDate : today;
        rule.setActiveRule(effectiveStart, endDate != null ? endDate : rule.getEndDate());

        DefaultMenuMaterializeResult materialized = null;
        if (!effectiveStart.isAfter(today)) {
            materialized = materializeIfAbsentForDate(menuGroup, today);
        }

        return DefaultMenuActivateResponse.builder()
                .defaultMenu(DefaultMenuResponse.from(rule, today))
                .materialized(materialized)
                .build();
    }

    private DefaultMenuMaterializeResult materializeIfAbsentForDate(MenuGroup group, LocalDate date) {
        GroupDailyMenu existing = groupDailyMenuRepository.findByMenuGroupIdAndDate(group.getId(), date)
                .orElse(null);
        if (existing != null) {
            log.debug("[DEFAULT_MENU][LAZY_MATERIALIZE] groupId={}, date={}, skipped=already_exists",
                    group.getId(), date);
            return DefaultMenuMaterializeResult.builder()
                    .date(date)
                    .replaced(false)
                    .itemCount(existing.getMenuNames().size())
                    .menus(existing.getMenuNames())
                    .build();
        }

        List<DefaultGroupMenu> activeDefaults = defaultGroupMenuRepository
                .findActiveByMenuGroupIdAndDate(group.getId(), date);

        Map<String, Boolean> merged = new LinkedHashMap<>();
        for (DefaultGroupMenu dgm : activeDefaults) {
            merged.putIfAbsent(dgm.getMenuName(), Boolean.TRUE);
        }

        List<String> menus = merged.keySet().stream().toList();

        GroupDailyMenu daily = GroupDailyMenu.builder()
                .menuGroup(group)
                .date(date)
                .build();
        daily.replaceMenus(menus);
        groupDailyMenuRepository.save(daily);

        log.debug("[DEFAULT_MENU][LAZY_MATERIALIZE] groupId={}, date={}, created=true, itemCount={}",
                group.getId(),
                date,
                menus.size());

        log.debug("[DEFAULT_MENU][MATERIALIZE] accountId={}, storeId={}, groupId={}, date={}, replaced={}, itemCount={}",
                currentAccountProvider.getCurrentAccountId(),
                group.getStore().getId(),
                group.getId(),
                date,
                false,
                menus.size());

        return DefaultMenuMaterializeResult.builder()
                .date(date)
                .replaced(false)
                .itemCount(menus.size())
                .menus(menus)
                .build();
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
