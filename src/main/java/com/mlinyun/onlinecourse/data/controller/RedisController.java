package com.mlinyun.onlinecourse.data.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Redis操作", description = "该接口为Redis操作接口，主要用来测试Redis的读写操作")
@RestController
@RequestMapping("/redis")
public class RedisController {

    /**
     * 使用 StringRedisTemplate 向 Redis 数据库中新增字符串数据以及读取操作
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 向 Redis 数据库新增一条记录
     * 主要用于测试 Redis 的写操作
     *
     * @param key   键
     * @param value 值
     * @return 存储结果
     */
    @ApiOperationSupport(author = "LingYun")
    @Operation(summary = "Redis的写操作", description = "向 Redis 数据库新增一条记录")
    @GetMapping("/addStringToRedis")
    @ResponseBody
    public Boolean addStringToRedis(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
        return true;
    }

    /**
     * 读取 Redis 数据库的一条记录
     * 主要用于测试 Redis 的读操作
     *
     * @param key 要读取记录的键
     * @return 读取的值
     */
    @ApiOperationSupport(author = "LingYun")
    @Operation(summary = "Redis的读操作", description = "向 Redis 数据库读取一条记录")
    @GetMapping("/getStringFromRedis")
    @ResponseBody
    public String getStringFromRedis(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
}
