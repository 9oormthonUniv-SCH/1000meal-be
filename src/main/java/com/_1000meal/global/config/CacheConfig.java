package com._1000meal.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        var shortTtl = Caffeine.newBuilder()
                .maximumSize(2_000)
                .expireAfterWrite(Duration.ofSeconds(60)) // 조회 API 캐시 60초
                .build();

        return new SimpleCacheManager() {{
            setCaches(List.of(
                    new CaffeineCache("stores:list", shortTtl),
                    new CaffeineCache("stores:detail", shortTtl)
            ));
        }};
    }
}