package com._1000meal.store.service;

import com._1000meal.menu.service.MenuGroupService;
import com._1000meal.store.dto.StoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreViewService {

    private final StoreService storeService;              // 원본 서비스 읽기 사용
    private final MenuGroupService menuGroupService;

    /** 1) 전체 목록 캐시(틀만 캐시) */
    @Cacheable(cacheNames = "stores:list", key = "'v1'", unless = "#result == null || #result.isEmpty()")
    public List<StoreResponse> getAllStoresCached() {
        return storeService.getAllStores(); // 원본 호출(캐시 저장)
    }

    /** 2) 뷰 응답: 캐시된 목록 + 재고만 실시간 DB로 덮어쓰기 */
    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStoresView() {
        var base = getAllStoresCached();
        var today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        var storeIds = base.stream().map(StoreResponse::getId).toList();
        var todayMenus = menuGroupService.getDailyMenuDtosForStores(storeIds, today);

        return base.stream().map(sr -> {
            var todayMenu = todayMenus.get(sr.getId());
            boolean isOpen = todayMenu != null ? todayMenu.isOpen() : sr.isOpen();
            boolean isHoliday = todayMenu != null && todayMenu.isHoliday();

            return sr.toBuilder()
                    .isOpen(isOpen)
                    .isHoliday(isHoliday)
                    .todayMenu(todayMenu)
                    .build();
        }).toList();
    }
}
