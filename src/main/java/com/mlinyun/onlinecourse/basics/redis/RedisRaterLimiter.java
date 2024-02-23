package com.mlinyun.onlinecourse.basics.redis;

import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 限流算法
 */
@Slf4j
@Component
public class RedisRaterLimiter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String INIT_TIMES = "1";

    private static final String LIMIT_MAX_PRE = "INS_LIMIT_MAX:";

    private static final String LIMIT_NOW_PRE = "INS_LIMIT_NOW:";

    public boolean getLimitFlag(@Parameter(name = "IP地址等唯一标识") String point, @Parameter(name = "限流次数") Integer limit, @Parameter(name = "限流单位时间") Long timeout) {
        if (limit < 1 || timeout < 1) {
            return true;
        }
        String maxCountKey = LIMIT_MAX_PRE + point;
        String currCountKey = LIMIT_NOW_PRE + point;
        try {
            // 限流次数
            String maxCount = stringRedisTemplate.opsForValue().get(maxCountKey);
            // 当前消耗次数
            String currCount = stringRedisTemplate.opsForValue().get(currCountKey);
            if (NullUtils.isNull(maxCount)) {
                // 重置次数
                stringRedisTemplate.opsForValue().set(currCountKey, INIT_TIMES, timeout, TimeUnit.MILLISECONDS);
                // 放入限制次数
                stringRedisTemplate.opsForValue().set(maxCountKey, limit + "", timeout, TimeUnit.MILLISECONDS);
                return true;
            } else if (!NullUtils.isNull(maxCount) && !NullUtils.isNull(currCount)) {
                if (Integer.valueOf(currCount) < Integer.valueOf(maxCount)) {
                    stringRedisTemplate.opsForValue().set(currCountKey, (Integer.valueOf(currCount) + 1) + "", timeout, TimeUnit.MILLISECONDS);
                    return true;
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("限流异常" + e);
        }
        return false;
    }

}
