package com.mlinyun.onlinecourse.basics.redis;

import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    @Schema(description = "单位时间限流个数")
    int limit() default 1000;

    @Schema(description = "单位时间毫秒数")
    long timeout() default 60000;

}
