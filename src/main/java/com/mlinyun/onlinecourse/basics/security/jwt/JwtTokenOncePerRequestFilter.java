package com.mlinyun.onlinecourse.basics.security.jwt;

import com.alibaba.fastjson2.JSONObject;
import com.mlinyun.onlinecourse.basics.baseVo.TokenUser;
import com.mlinyun.onlinecourse.basics.parameter.SysLoginProperties;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.ResponseUtil;
import com.mlinyun.onlinecourse.basics.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 自定义权限过滤
 */
@Schema(description = "自定义权限过滤")
@Slf4j
public class JwtTokenOncePerRequestFilter extends OncePerRequestFilter {

    @Resource
    private SecurityUtil securityUtil;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @Resource
    private SysLoginProperties sysLoginProperties;

    private static final boolean RESPONSE_FAIL_FLAG = false;

    private static final int RESPONSE_NO_ROLE_CODE = 401;


    @Operation(summary = "自定义权限过滤")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader(SysLoginProperties.HTTP_HEADER);
        if (NullUtils.isNull(tokenHeader)) {
            tokenHeader = request.getParameter(SysLoginProperties.HTTP_HEADER);
        }
        if (NullUtils.isNull(tokenHeader)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            UsernamePasswordAuthenticationToken token = getUsernamePasswordAuthenticationToken(tokenHeader, response);
            SecurityContextHolder.getContext().setAuthentication(token);
        } catch (Exception e) {
            log.warn("自定义权限过滤失败" + e);
        }
        filterChain.doFilter(request, response);
    }

    @Operation(summary = "判断登录是否失效")
    private UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(String header, HttpServletResponse response) {
        String userName = null;
        String tokenInRedis = redisTemplateHelper.get(SysLoginProperties.HTTP_TOKEN_PRE + header);
        if (NullUtils.isNull(tokenInRedis)) {
            ResponseUtil.out(response, ResponseUtil.resultMap(RESPONSE_FAIL_FLAG, RESPONSE_NO_ROLE_CODE, "登录状态失效，需要重登！"));
            return null;
        }

        TokenUser tokenUser = JSONObject.parseObject(tokenInRedis, TokenUser.class);
        userName = tokenUser.getUsername();
        List<GrantedAuthority> permissionList = new ArrayList<>();
        if (sysLoginProperties.getSaveRoleFlag()) {
            for (String permission : tokenUser.getPermissions()) {
                permissionList.add(new SimpleGrantedAuthority(permission));
            }
        } else {
            permissionList = securityUtil.getCurrUserPerms(userName);
        }
        if (!tokenUser.getSaveLogin()) {
            redisTemplateHelper.set(SysLoginProperties.USER_TOKEN_PRE + userName, header, sysLoginProperties.getUserTokenInvalidDays(), TimeUnit.MINUTES);
            redisTemplateHelper.set(SysLoginProperties.HTTP_TOKEN_PRE + header, tokenInRedis, sysLoginProperties.getUserTokenInvalidDays(), TimeUnit.MINUTES);
        }
        if (!NullUtils.isNull(userName)) {
            User user = new User(userName, "", permissionList);
            return new UsernamePasswordAuthenticationToken(user, null, permissionList);
        }
        return null;
    }

    public JwtTokenOncePerRequestFilter(RedisTemplateHelper redis, SecurityUtil securityUtil, SysLoginProperties sysLoginProperties) {
        this.redisTemplateHelper = redis;
        this.securityUtil = securityUtil;
        this.sysLoginProperties = sysLoginProperties;
    }

}
