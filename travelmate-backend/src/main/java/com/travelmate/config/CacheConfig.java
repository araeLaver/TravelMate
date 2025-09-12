package com.travelmate.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // 기본 TTL 10분
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();
        
        // 캐시별 개별 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 사용자 프로필 캐시 (30분)
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 사용자 위치 캐시 (5분)
        cacheConfigurations.put("user_locations", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 근처 사용자 캐시 (2분)
        cacheConfigurations.put("nearby_users", defaultConfig.entryTtl(Duration.ofMinutes(2)));
        
        // 사용자 검색 결과 캐시 (10분)
        cacheConfigurations.put("user_search", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 사용자 리뷰 캐시 (1시간)
        cacheConfigurations.put("user_reviews", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 사용자 통계 캐시 (30분)
        cacheConfigurations.put("user_stats", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 여행 그룹 캐시 (15분)
        cacheConfigurations.put("travel_groups", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // 추천 시스템 캐시 (1시간)
        cacheConfigurations.put("recommendations", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}