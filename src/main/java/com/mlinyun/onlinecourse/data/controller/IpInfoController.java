package com.mlinyun.onlinecourse.data.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.IpInfoUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "IP接口")
@RequestMapping("/ip")
@CacheConfig(cacheNames = "ip")
@RestController
@Transactional
public class IpInfoController {

    @Resource
    private IpInfoUtil ipInfoUtil;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "IP信息", logType = LogType.DATA_CENTER, doType = "IP-01")
    @Operation(summary = "IP信息", description = "获取IP信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Result<Object> upload(HttpServletRequest request) {
        return ResultUtil.data(ipInfoUtil.getIpCity(request));
    }

}