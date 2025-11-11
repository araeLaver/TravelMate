package com.travelmate.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisCacheConfig {

    /**
     * Redis 캐시 매니저 설정
     * - 각 캐시별로 다른 TTL 설정 가능
     * - JSON 직렬화 사용
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 기본 TTL: 30분
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues(); // null 값은 캐싱하지 않음

        // 캐시별 커스텀 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 사용자 정보: 10분
        cacheConfigurations.put("users",
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 여행 그룹 목록: 5분
        cacheConfigurations.put("travelGroups",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 여행 그룹 상세: 10분
        cacheConfigurations.put("travelGroupDetails",
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 추천 데이터: 15분 (자주 변경되지 않음)
        cacheConfigurations.put("recommendations",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // 검색 결과: 3분 (자주 변경될 수 있음)
        cacheConfigurations.put("searchResults",
                defaultConfig.entryTtl(Duration.ofMinutes(3)));

        // 채팅방 목록: 5분
        cacheConfigurations.put("chatRooms",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 게시글 목록: 5분
        cacheConfigurations.put("posts",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 통계 데이터: 30분
        cacheConfigurations.put("statistics",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 트랜잭션 지원
                .build();
    }

    /**
     * Redis Template 설정
     * - 일반적인 Redis 작업에 사용
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value Serializer (JSON)
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
