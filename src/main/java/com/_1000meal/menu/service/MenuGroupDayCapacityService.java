package com._1000meal.menu.service;

import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.domain.MenuGroupDayCapacity;
import com._1000meal.menu.dto.MenuGroupDayCapacityAdminResponse;
import com._1000meal.menu.dto.MenuGroupDayCapacityUpdateRequest;
import com._1000meal.menu.repository.MenuGroupDayCapacityRepository;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MenuGroupDayCapacityService {

    private final MenuGroupRepository menuGroupRepository;
    private final MenuGroupDayCapacityRepository menuGroupDayCapacityRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public MenuGroupDayCapacityAdminResponse updateCapacitiesForAdmin(
            Long storeId,
            Long groupId,
            MenuGroupDayCapacityUpdateRequest request
    ) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        MenuGroup menuGroup = menuGroupRepository.findByIdAndStoreId(groupId, storeId)
                .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_GROUP_NOT_FOUND));

        List<MenuGroupDayCapacityUpdateRequest.DayCapacityItem> items = request.getCapacities();
        Set<DayOfWeek> seen = new HashSet<>();
        for (MenuGroupDayCapacityUpdateRequest.DayCapacityItem item : items) {
            if (!seen.add(item.getDayOfWeek())) {
                throw new CustomException(MenuErrorCode.MENU_GROUP_DAY_CAPACITY_DUPLICATE_DAY);
            }
        }

        for (MenuGroupDayCapacityUpdateRequest.DayCapacityItem item : items) {
            DayOfWeek dow = item.getDayOfWeek();
            int cap = item.getCapacity();
            menuGroupDayCapacityRepository.findByMenuGroupIdAndDayOfWeek(menuGroup.getId(), dow)
                    .ifPresentOrElse(
                            row -> row.updateCapacity(cap),
                            () -> menuGroupDayCapacityRepository.save(MenuGroupDayCapacity.create(menuGroup, dow, cap))
                    );
        }

        List<MenuGroupDayCapacity> all = menuGroupDayCapacityRepository.findByMenuGroupId(menuGroup.getId());
        return MenuGroupDayCapacityAdminResponse.of(storeId, groupId, all);
    }
}
