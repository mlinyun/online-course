package com.mlinyun.onlinecourse.basics.redis;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

import java.io.Serial;
import java.io.Serializable;

/**
 * Redis锁工具类
 */
@Configuration
public class RedisLockConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Bean
    @Operation(summary = "初始化Redis锁")
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockRegistry(redisConnectionFactory, "online-course-lock");
    }

}
