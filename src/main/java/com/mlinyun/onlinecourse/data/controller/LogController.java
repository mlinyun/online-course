package com.mlinyun.onlinecourse.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.PageUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.entity.Log;
import com.mlinyun.onlinecourse.data.service.ILogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "日志管理接口")
@RequestMapping("/log")
@CacheConfig(cacheNames = "log")
@RestController
@Transactional
public class LogController {

    @Resource
    private ILogService iLogService;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询日志", logType = LogType.DATA_CENTER, doType = "LOG-01")
    @Operation(summary = "查询日志", description = "查询日志")
    @RequestMapping(value = "/getAllByPage", method = RequestMethod.GET)
    public Result<Object> getAllByPage(@ModelAttribute Log log, @ModelAttribute PageVo page) {
        QueryWrapper<Log> qw = new QueryWrapper<>();
        if (!NullUtils.isNull(log.getName())) {
            qw.like("name", log.getName());
        }
        if (log.getLogType() != null) {
            qw.eq("logType", log.getLogType());
        }
        if (!NullUtils.isNull(log.getUsername())) {
            qw.like("username", log.getUsername());
        }
        if (!NullUtils.isNull(log.getIp())) {
            qw.like("ip", log.getIp());
        }
        if (!NullUtils.isNull(log.getStartDate())) {
            qw.ge("createTime", log.getStartDate());
            qw.le("createTime", log.getEndDate());
        }
        return ResultUtil.data(iLogService.page(PageUtil.initMpPage(page), qw));
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "删除日志", logType = LogType.DATA_CENTER, doType = "LOG-02")
    @Operation(summary = "删除日志", description = "删除日志")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result<Object> delByIds(@RequestParam String[] ids) {
        for (String logId : ids) {
            iLogService.removeById(logId);
        }
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "删除全部日志", logType = LogType.DATA_CENTER, doType = "LOG-03")
    @Operation(summary = "删除全部日志", description = "删除全部日志")
    @RequestMapping(value = "/delAll", method = RequestMethod.POST)
    public Result<Object> delAll() {
        iLogService.remove(new QueryWrapper<Log>());
        return ResultUtil.success();
    }

}
