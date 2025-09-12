package com._1000meal.store.service;

import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.menu.service.MenuService;
import com._1000meal.store.dto.StoreDetailedResponse;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Primary // ← 기존 StoreService 대신 이 빈이 기본 주입됨(코드 수정 無)
public class CachedStoreService extends StoreService {

    public CachedStoreService(StoreRepository storeRepository,
                              DailyMenuRepository dailyMenuRepository,
                              MenuService menuService) {
        super(storeRepository, dailyMenuRepository, menuService);
    }

    /** 전체 매장 조회 + 오늘 메뉴 → 60초 캐시 */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "stores:list", key = "'v1'", unless = "#result == null || #result.isEmpty()")
    public List<StoreResponse> getAllStores() {
        return super.getAllStores();
    }

    /** 매장 상세 → 60초 캐시 (매장별 키) */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "stores:detail", key = "#storeId", unless = "#result == null")
    public StoreDetailedResponse getStoreDetail(Long storeId) {
        return super.getStoreDetail(storeId);
    }

    /** 상태 토글은 캐시 무효화(보수적으로 전량) */
    @Override
    @Transactional
    @CacheEvict(cacheNames = {"stores:list", "stores:detail"}, allEntries = true)
    public String toggleStoreStatus(Long storeId) {
        String msg = super.toggleStoreStatus(storeId);
        log.debug("Evicted caches after toggleStoreStatus(storeId={})", storeId);
        return msg;
    }

    /** 이미지 변경도 캐시 무효화(전량) */
    @Override
    @Transactional
    @CacheEvict(cacheNames = {"stores:list", "stores:detail"}, allEntries = true)
    public String setImageUrl(Long storeId, String imageUrl) {
        String url = super.setImageUrl(storeId, imageUrl);
        log.debug("Evicted caches after setImageUrl(storeId={})", storeId);
        return url;
    }
}