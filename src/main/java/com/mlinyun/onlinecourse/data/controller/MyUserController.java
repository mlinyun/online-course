package com.mlinyun.onlinecourse.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.PageUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.entity.User;
import com.mlinyun.onlinecourse.data.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "新用户接口")
@RequestMapping("/myUser")
@CacheConfig(cacheNames = "myUser")
@RestController
@Transactional
public class MyUserController {

    @Resource
    private IUserService iUserService;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询用户", logType = LogType.DATA_CENTER, doType = "USER-01")
    @Operation(summary = "查询用户", description = "查询用户")
    @RequestMapping(value = "/getByPage", method = RequestMethod.GET)
    public Result<IPage<User>> getByPage(@ModelAttribute User user, @ModelAttribute PageVo page) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (user.getDepartmentId() != null && !NullUtils.isNull(user.getDepartmentId())) {
            qw.like("departmentId", user.getDepartmentId());
        }
        if (user.getNickname() != null && !NullUtils.isNull(user.getNickname())) {
            qw.like("nickname", user.getNickname());
        }
        IPage<User> data = iUserService.page(PageUtil.initMpPage(page), qw);
        return new ResultUtil<IPage<User>>().setData(data);
    }

}
