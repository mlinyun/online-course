package com.mlinyun.onlinecourse.basics.redis;

import com.google.gson.Gson;
import com.mlinyun.onlinecourse.basics.exception.RuntimeExceptionHandler;
import com.mlinyun.onlinecourse.basics.parameter.SysLoginProperties;
import com.mlinyun.onlinecourse.basics.utils.IpInfoUtil;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.data.entity.Setting;
import com.mlinyun.onlinecourse.data.service.ISettingService;
import com.mlinyun.onlinecourse.data.vo.HttpIpSsoSetting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 限流拦截器
 */
@Slf4j
@Component
public class LimitRaterInterceptor implements HandlerInterceptor {

    @Resource
    private SysLoginProperties sysLoginProperties;

    @Resource
    private RedisRaterLimiter redisRaterLimiter;

    @Resource
    private IpInfoUtil ipInfoUtil;

    @Resource
    private ISettingService iSettingService;

    private static final String OTHER_SETTING = "OTHER_SETTING";

    @Operation(summary = "查询系统黑名单配置")
    public HttpIpSsoSetting getHttpIpSsoSetting() {
        Setting s = iSettingService.getById(OTHER_SETTING);
        if (s != null && !NullUtils.isNull(s.getValue())) {
            return new Gson().fromJson(s.getValue(), HttpIpSsoSetting.class);
        }
        return null;
    }

    @Override
    @Operation(summary = "方法执行前过滤")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, @Parameter(name = "响应的处理器") Object handler) throws Exception {
        String ip = ipInfoUtil.getRequestIpAddress(request);
        // 单IP限流判断
        if (sysLoginProperties.getOneLimiting()) {
            boolean flag1 = redisRaterLimiter.getLimitFlag(ip, sysLoginProperties.getOneLimitingSize(), sysLoginProperties.getOneLimitingTime());
            if (!flag1) {
                throw new RuntimeExceptionHandler("您的请求过于频繁，请稍后再试！");
            }
        }
        // 全局限流判断
        if (sysLoginProperties.getAllLimiting()) {
            boolean flag2 = redisRaterLimiter.getLimitFlag("SYS_LIMIT_ALL", sysLoginProperties.getAllLimitingSize(), sysLoginProperties.getAllLimitingTime());
            if (!flag2) {
                throw new RuntimeExceptionHandler("系统已达到最大承载量，无法继续提供服务，请稍后再试！");
            }
        }
        // IP黑名单
        HttpIpSsoSetting os = getHttpIpSsoSetting();
        if (os != null && !NullUtils.isNull(os.getBlacklist())) {
            String[] list = os.getBlacklist().split("\n");
            for (String item : list) {
                if (Objects.equals(ip, item)) {
                    throw new RuntimeExceptionHandler("您已被禁止访问该系统，如有疑问请咨询管理员，谢谢！");
                }
            }
        }
        // 特定方法限流判断
        try {
            Method method = ((HandlerMethod) handler).getMethod();
            RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
            if (rateLimiter != null) {
                boolean flag3 = redisRaterLimiter.getLimitFlag(method.getName(), rateLimiter.limit(), rateLimiter.timeout());
                if (!flag3) {
                    throw new RuntimeExceptionHandler(method.getName() + "方法请求超限，请稍后再试");
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        // 放行至下一个拦截器
        return true;
    }

    @Override
    @Operation(summary = "方法执行完后过滤")
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }

    @Override
    @Operation(summary = "请求返回后过滤")
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

    }

}
