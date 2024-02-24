package com.mlinyun.onlinecourse.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.basics.security.SecurityUserDetails;
import com.mlinyun.onlinecourse.basics.utils.WeiChatUtils;
import com.mlinyun.onlinecourse.basics.utils.WxNoticeUtils;
import com.mlinyun.onlinecourse.basics.utils.CommonUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.basics.utils.SecurityUtil;
import com.mlinyun.onlinecourse.data.entity.User;
import com.mlinyun.onlinecourse.data.service.IUserService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@RestController
@Tag(name = "企业微信验证码登录接口")
@RequestMapping("/qwVerificationCode")
@Transactional
public class QwVerificationCodeController {

    @Resource
    private IUserService iUserService;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @Resource
    private SecurityUtil securityUtil;

    @Schema(description = "发送企微验证码")
    @RequestMapping(value = "/sendVerificationCode", method = RequestMethod.GET)
    public Result<Object> sendVerificationCode(@RequestParam String jobNumber) {
        if (!Objects.equals("zwz", jobNumber)) {
            return ResultUtil.error("请联系管理员配置您的工号");
        }
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("status", 0);
        userQw.eq("username", jobNumber);
        Long userCount = iUserService.count(userQw);
        if (userCount < 1L) {
            return new ResultUtil<Object>().setErrorMsg("无权限登入");
        }
        String verificationCode = CommonUtil.getRandomTwoNum();
        /**
         * 这里需要实现判断发给谁的业务逻辑
         */
        WxNoticeUtils.sendTuWenMessage("zwz", "OA登录验证", "验证码 " + verificationCode + "，1分钟后失效", "https://gitee.com/yyzwz", "https://bkimg.cdn.bcebos.com/pic/37d12f2eb9389b503a80d4b38b35e5dde6116ed7", WeiChatUtils.getToken());
        redisTemplateHelper.set("qwsms:" + jobNumber, verificationCode, 60, TimeUnit.SECONDS);
        return ResultUtil.success();
    }

    @SystemLog(logName = "企微验证码登入", logType = LogType.LOGIN)
    @Schema(description = "企微验证码登入")
    @RequestMapping(value = "/verificationCodeLogin", method = RequestMethod.GET)
    public Result<Object> verificationCodeLogin(@RequestParam String jobNumber, @RequestParam String code) {
        String codeAns = redisTemplateHelper.get("qwsms:" + jobNumber);
        if (codeAns == null) {
            return ResultUtil.error("验证码已过期");
        }
        if (codeAns.equals(code)) {
            QueryWrapper<User> userQw = new QueryWrapper<>();
            userQw.eq("username", jobNumber);
            List<User> users = iUserService.list(userQw);
            if (users.isEmpty()) {
                return ResultUtil.error(jobNumber + "账户不存在");
            }
            String accessToken = securityUtil.getToken(jobNumber, false);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(new SecurityUserDetails(users.get(0)), null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResultUtil.data(accessToken);
        }
        return ResultUtil.error("验证码错误");
    }

}
