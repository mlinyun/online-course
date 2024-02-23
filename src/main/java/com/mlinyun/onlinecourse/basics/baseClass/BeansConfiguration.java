package com.mlinyun.onlinecourse.basics.baseClass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * SpringBoot 启动配置类
 */
@Configuration
@Slf4j
@Schema(description = "SpringBoot 启动配置类")
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class BeansConfiguration {

    @Bean
    @Operation(summary = "初始化PathMatcher")
    public PathMatcher pathMatcher() {
        log.info("初始化PathMatcher成功");
        return new AntPathMatcher();
    }

    @Primary
    @Bean
    @Operation(summary = "初始化线程池")
    public TaskExecutor primaryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        log.info("初始化线程池成功");
        return executor;
    }

    @Bean
    @Operation(summary = "初始化Redis锁")
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockRegistry(redisConnectionFactory, "online-course-lock");
    }
}
