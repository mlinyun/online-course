package com.mlinyun.onlinecourse.basics.redis;

import com.mlinyun.onlinecourse.basics.parameter.NoAuthenticationProperties;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 */
@Slf4j
@Configuration
public class InterceptConfiguration implements WebMvcConfigurer {

    @Resource
    private NoAuthenticationProperties noAuthenticationProperties;

    @Resource
    private LimitRaterInterceptor limitRaterInterceptor;

    private static final String PATH_PATTERN_STR = "/**";

    @Override
    @Operation(summary = "重写鉴权拦截逻辑")
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(limitRaterInterceptor);
        registration.addPathPatterns(PATH_PATTERN_STR);
        registration.excludePathPatterns(noAuthenticationProperties.getLimiting());
        log.info("拦截器加载成功");
    }

}
