package com.mlinyun.onlinecourse.data.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公共接口")
@RequestMapping("/common")
@CacheConfig(cacheNames = "common")
@RestController
@Transactional
public class SecurityController {

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "未登录返回的数据", logType = LogType.DATA_CENTER, doType = "SEC-01")
    @Operation(summary = "未登录返回的数据", description = "未登录返回的数据")
    @RequestMapping(value = "/needLogin", method = RequestMethod.GET)
    public Result<Object> needLogin() {
        return ResultUtil.error(401, "登录失效");
    }

}
