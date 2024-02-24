package com.mlinyun.onlinecourse.basics.security.jwt;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mlinyun.onlinecourse.basics.baseVo.TokenUser;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.parameter.SysLoginProperties;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录成功回调
 */
@Schema(description = "登录成功回调")
@Slf4j
@Component
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Resource
    private SysLoginProperties tokenProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final boolean RESPONSE_SUCCESS_FLAG = true;

    private static final int RESPONSE_SUCCESS_CODE = 200;

    private static final String TOKEN_REPLACE_STR_FRONT = "-";

    private static final String TOKEN_REPLACE_STR_BACK = "";

    @Override
    @Schema(description = "登录成功回调")
    @SystemLog(logName = "登录系统", logType = LogType.LOGIN)
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication ac) throws IOException, ServletException {
        String saveLogin = request.getParameter(SysLoginProperties.SAVE_LOGIN_PRE);
        Boolean saveLoginFlag = false;
        if (!NullUtils.isNull(saveLogin) && Objects.equals(saveLogin, "true")) {
            saveLoginFlag = true;
        }
        List<String> permissionsList = new ArrayList<>();
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) ((UserDetails) ac.getPrincipal()).getAuthorities();
        for (GrantedAuthority g : authorities) {
            permissionsList.add(g.getAuthority());
        }
        String token = UUID.randomUUID().toString().replace(TOKEN_REPLACE_STR_FRONT, TOKEN_REPLACE_STR_BACK);
        String username = ((UserDetails) ac.getPrincipal()).getUsername();
        TokenUser user = new TokenUser(username, permissionsList, saveLoginFlag);
        // 判断是否存储菜单权限
        if (!tokenProperties.getSaveRoleFlag()) {
            user.setPermissions(null);
        }
        // 单点登录判断
        if (tokenProperties.getSsoFlag()) {
            String oldToken = stringRedisTemplate.opsForValue().get(SysLoginProperties.USER_TOKEN_PRE + username);
            if (StrUtil.isNotBlank(oldToken)) {
                stringRedisTemplate.delete(SysLoginProperties.HTTP_TOKEN_PRE + oldToken);
            }
        }
        if (saveLoginFlag) {
            stringRedisTemplate.opsForValue().set(SysLoginProperties.USER_TOKEN_PRE + username, token, tokenProperties.getUserSaveLoginTokenDays(), TimeUnit.DAYS);
            stringRedisTemplate.opsForValue().set(SysLoginProperties.HTTP_TOKEN_PRE + token, JSON.toJSONString(user), tokenProperties.getUserSaveLoginTokenDays(), TimeUnit.DAYS);
        } else {
            stringRedisTemplate.opsForValue().set(SysLoginProperties.USER_TOKEN_PRE + username, token, tokenProperties.getUserTokenInvalidDays(), TimeUnit.MINUTES);
            stringRedisTemplate.opsForValue().set(SysLoginProperties.HTTP_TOKEN_PRE + token, JSON.toJSONString(user), tokenProperties.getUserTokenInvalidDays(), TimeUnit.MINUTES);
        }
        ResponseUtil.out(response, ResponseUtil.resultMap(RESPONSE_SUCCESS_FLAG, RESPONSE_SUCCESS_CODE, "登录成功", token));
    }
}
