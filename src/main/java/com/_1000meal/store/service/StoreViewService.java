package com._1000meal.store.service;

import com._1000meal.menu.repository.DailyMenuRepository;
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
    private final DailyMenuRepository dailyMenuRepository;

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

        return base.stream().map(sr -> {
            Long storeId = sr.getId(); // DTO 실제 접근자에 맞게 수정
            Integer liveStock = dailyMenuRepository
                    .findTotalGroupStockByStoreIdAndDate(storeId, today)
                    .orElse(sr.getTodayMenu() != null ? sr.getTodayMenu().getStock() : null);

            // DTO가 불변이면 복사 생성/빌더 필요
            var tm = sr.getTodayMenu();
            var tmUpdated = (tm == null) ? null
                    : tm.toBuilder()
                    .stock(liveStock)  // 필드명 맞추기
                    .build();

            return sr.toBuilder()
                    .todayMenu(tmUpdated) // 필드명 맞추기
                    .build();
        }).toList();
    }
}