package com._1000meal.store.service;

import com._1000meal.menu.repository.DailyMenuRepository;
import com._1000meal.store.dto.StoreResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = StoreViewServiceCacheIT.TestConfig.class)
class StoreViewServiceCacheIT {

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("stores:list");
        }
        @Bean StoreViewService storeViewService(StoreService storeService, DailyMenuRepository dailyMenuRepository) {
            return new StoreViewService(storeService, dailyMenuRepository);
        }
    }

    @MockBean private StoreService storeService;
    @MockBean private DailyMenuRepository dailyMenuRepository;

    @Autowired
    private StoreViewService storeViewService;

    @Test
    @DisplayName("getAllStoresCached: 같은 key면 캐시로 인해 StoreService.getAllStores는 1번만 호출된다")
    void getAllStoresCached_cacheWorks() {
        when(storeService.getAllStores()).thenReturn(List.of(
                StoreResponse.builder().id(1L).build()
        ));

        storeViewService.getAllStoresCached();
        storeViewService.getAllStoresCached();

        verify(storeService, times(1)).getAllStores();
    }
}