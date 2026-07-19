package com.talentFlow.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisCacheConfig implements CachingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);
    private static final String CACHE_PREFIX = "talent-flow:v4::";

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper redisObjectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> CACHE_PREFIX + cacheName + "::")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache GET failed for cache='{}' key='{}'. Falling through to source. Error: {}",
                    cache.getName(), key, exception.getMessage());
                try {
                    cache.evict(key);
                } catch (Exception e) {
                    log.warn("Failed to evict stale cache entry for cache='{}' key='{}'", cache.getName(), key);
                }
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache PUT failed for cache='{}' key='{}'. Data was saved to DB; continuing. Error: {}",
                    cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache EVICT failed for cache='{}' key='{}'. DB operation already committed; continuing. Error: {}",
                    cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache CLEAR failed for cache='{}'. DB operation already committed; continuing. Error: {}",
                    cache.getName(), exception.getMessage());
            }
        };
    }
}
