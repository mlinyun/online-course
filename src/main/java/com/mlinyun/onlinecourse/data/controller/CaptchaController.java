package com.mlinyun.onlinecourse.data.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.utils.CreateVerifyCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "验证码接口")
@RequestMapping("/common/captcha")
@CacheConfig(cacheNames = "captcha")
@RestController
@Transactional
public class CaptchaController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "初始化验证码", logType = LogType.DATA_CENTER, doType = "CAP-01")
    @Operation(summary = "初始化验证码", description = "用于初始化验证码，返回验证码ID")
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public Result<Object> init() {
        String codeId = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(codeId, new CreateVerifyCode().randomStr(4), 2L, TimeUnit.MINUTES);
        return ResultUtil.data(codeId);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "根据验证码ID获取图片", logType = LogType.DATA_CENTER, doType = "CAP-02")
    @Operation(summary = "根据验证码ID获取图片", description = "根据验证码ID创建验证码图片")
    @RequestMapping(value = "/draw/{captchaId}", method = RequestMethod.GET)
    public void draw(@PathVariable("captchaId") String captchaId, HttpServletResponse response) throws IOException {
        String codeStr = stringRedisTemplate.opsForValue().get(captchaId);
        CreateVerifyCode createVerifyCode = new CreateVerifyCode(116, 36, 4, 10, codeStr);
        response.setContentType("image/png");
        createVerifyCode.write(response.getOutputStream());
    }

}
