package com.mlinyun.onlinecourse.data.controller;

import cn.hutool.core.date.DateUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.PageUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.vo.RedisInfo;
import com.mlinyun.onlinecourse.data.vo.RedisVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Tag(name = "Redis缓存管理接口")
@RequestMapping("/redis")
@CacheConfig(cacheNames = "redis")
@RestController
@Transactional
public class RedisController {

    @Schema(description = "最大键值数")
    private static final int maxSize = 100000;

    private static final String DATE_FORMAT_IN_REDIS = "HH:mm:ss";

    private static final String STEP_STR_IN_REDIS = "*";

    private static final Integer INIT_SIZE_IN_REDIS = 16;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "新增", logType = LogType.DATA_CENTER, doType = "REDIS-01")
    @Operation(summary = "新增", description = "新增")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Result<Object> save(@RequestParam String key, @RequestParam String value, @RequestParam Long expireTime) {
        if (expireTime == 0L) {
            return ResultUtil.success();
        }
        if (expireTime < 0) {
            stringRedisTemplate.opsForValue().set(key, value);
        }
        stringRedisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "删除", logType = LogType.DATA_CENTER, doType = "REDIS-02")
    @Operation(summary = "删除", description = "删除")
    @RequestMapping(value = "/delByKeys", method = RequestMethod.POST)
    public Result<Object> delByKeys(@RequestParam String[] keys) {
        for (String redisKey : keys) {
            stringRedisTemplate.delete(redisKey);
        }
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "全部删除", logType = LogType.DATA_CENTER, doType = "REDIS-03")
    @Operation(summary = "全部删除", description = "全部删除")
    @RequestMapping(value = "/delAll", method = RequestMethod.POST)
    public Result<Object> delAll() {
        stringRedisTemplate.delete(redisTemplateHelper.keys(STEP_STR_IN_REDIS));
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "获取实时key大小", logType = LogType.DATA_CENTER, doType = "REDIS-04")
    @Operation(summary = "获取实时key大小", description = "获取实时key大小")
    @RequestMapping(value = "/getKeySize", method = RequestMethod.GET)
    public Result<Object> getKeySize() {
        Map<String, Object> map = new HashMap<>(INIT_SIZE_IN_REDIS);
        map.put("keySize", stringRedisTemplate.getConnectionFactory().getConnection().dbSize());
        map.put("time", DateUtil.format(new Date(), DATE_FORMAT_IN_REDIS));
        return ResultUtil.data(map);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "获取实时内存大小", logType = LogType.DATA_CENTER, doType = "REDIS-05")
    @Operation(summary = "获取实时内存大小", description = "获取实时内存大小")
    @RequestMapping(value = "/getMemory", method = RequestMethod.GET)
    public Result<Object> getMemory() {
        Map<String, Object> hashMap = new HashMap<>(INIT_SIZE_IN_REDIS);
        Properties properties = stringRedisTemplate.getConnectionFactory().getConnection().info("memory");
        hashMap.put("memory", properties.get("used_memory"));
        hashMap.put("time", DateUtil.format(new Date(), DATE_FORMAT_IN_REDIS));
        return ResultUtil.data(hashMap);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询Redis数据", logType = LogType.DATA_CENTER, doType = "REDIS-06")
    @Operation(summary = "查询Redis数据", description = "查询Redis数据")
    @RequestMapping(value = "/getAllByPage", method = RequestMethod.GET)
    public Result<Page<RedisVO>> getAllByPage(@RequestParam(required = false) String key, PageVo pageVo) {
        List<RedisVO> list = new ArrayList<>();
        if (!NullUtils.isNull(key)) {
            key = STEP_STR_IN_REDIS + key + STEP_STR_IN_REDIS;
        } else {
            key = STEP_STR_IN_REDIS;
        }
        Set<String> keyListInSet = redisTemplateHelper.keys(key);
        int keyListInSetSize = keyListInSet.size();
        if (keyListInSetSize > maxSize) {
            keyListInSetSize = maxSize;
        }
        int i = 0;
        for (String keyInSet : keyListInSet) {
            if (i > keyListInSetSize) {
                break;
            }
            RedisVO redisVo = new RedisVO(keyInSet, "", stringRedisTemplate.getExpire(keyInSet, TimeUnit.SECONDS));
            list.add(redisVo);
            i++;
        }
        Page<RedisVO> page = new PageImpl<RedisVO>(PageUtil.listToPage(pageVo, list), PageUtil.initPage(pageVo), keyListInSetSize);
        for (RedisVO vo : page.getContent()) {
            String ansValue = null;
            try {
                ansValue = stringRedisTemplate.opsForValue().get(vo.getKey());
                if (ansValue.length() > 100) {
                    ansValue = ansValue.substring(0, 100) + "..";
                }
                vo.setValue(ansValue);
            } catch (Exception ex) {
                vo.setValue("二进制内容");
            }
        }
        return new ResultUtil<Page<RedisVO>>().setData(page);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "获取Redis信息", logType = LogType.DATA_CENTER, doType = "REDIS-07")
    @Operation(summary = "获取Redis信息", description = "获取Redis信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Result<Object> info() {
        List<RedisInfo> redisInfoList = new ArrayList<>();
        Properties properties = stringRedisTemplate.getConnectionFactory().getConnection().info();
        Set<Object> keys = properties.keySet();
        for (Object key : keys) {
            String valueInRedis = properties.get(key).toString();
            RedisInfo ri = new RedisInfo();
            ri.setKey(key.toString());
            ri.setValue(valueInRedis);
            redisInfoList.add(ri);
        }
        return ResultUtil.data(redisInfoList);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "通过key获取", logType = LogType.DATA_CENTER, doType = "REDIS-08")
    @Operation(summary = "通过key获取", description = "通过key获取")
    @RequestMapping(value = "/getByKey/{key}", method = RequestMethod.GET)
    public Result<Object> getByKey(@PathVariable String key) {
        Map<String, Object> map = new HashMap<>();
        String redisValue = stringRedisTemplate.opsForValue().get(key);
        Long expireTimeUnit = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        map.put("value", redisValue);
        map.put("expireTime", expireTimeUnit);
        return ResultUtil.data(map);
    }

}
