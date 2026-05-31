/**
 * Caffeine 캐시 설정
 * 실시간 주차 데이터를 30초간 메모리에 캐싱
 * → 매 요청마다 DB 조회하지 않아도 됨
 */
package com.parking.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                "realtimeSpots",    // 실시간 잔여 공간 캐시
                "parkingDetail"     // 주차장 상세 정보 캐시
        );
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.SECONDS)  // 30초 후 만료
                        .maximumSize(1000)                        // 최대 1000개
        );
        return manager;
    }
}