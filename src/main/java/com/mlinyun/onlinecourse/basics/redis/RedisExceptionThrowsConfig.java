package com.mlinyun.onlinecourse.basics.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Objects;

/**
 * Redis异常处理
 */
@Configuration
@Slf4j
public class RedisExceptionThrowsConfig implements CachingConfigurer {

    @Schema(description = "时长类型")
    private String unit = "day";

    @Schema(name = "时长值", description = "-1为不限制")
    private Integer time = 30;

    @Override
    @Operation(summary = "Redis序列化异常")
    public CacheErrorHandler errorHandler() {
        CacheErrorHandler ceh = new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("Redis序列化出现了查询异常");
                log.warn(key.toString());
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Redis序列化出现了插入异常");
                log.warn(key.toString());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("Redis序列化出现了Evict异常");
                log.warn(key.toString());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("Redis序列化出现了删除异常");
            }
        };
        return ceh;
    }

    @Bean
    @Operation(summary = "Redis序列化")
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        // 判断缓存格式化错误
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        PolymorphicTypeValidator ptv = new ObjectMapper().getPolymorphicTypeValidator();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(objectMapper, Object.class);

        // 判断乱码错误
        RedisCacheConfiguration rc = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();
        // 处理缓存时长
        Duration expireTime = Duration.ofDays(time);
        if (Objects.equals(unit, "hour")) {
            expireTime = Duration.ofHours(time);
        } else if (Objects.equals(unit, "minute")) {
            expireTime = Duration.ofMinutes(time);
        }
        return RedisCacheManager.builder(factory).cacheDefaults(rc.entryTtl(expireTime)).build();
    }
}

