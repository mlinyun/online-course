package com.mlinyun.onlinecourse.basics.security;

import com.mlinyun.onlinecourse.basics.parameter.NoAuthenticationProperties;
import com.mlinyun.onlinecourse.basics.parameter.SysLoginProperties;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.basics.security.jwt.AuthenticationFailHandler;
import com.mlinyun.onlinecourse.basics.security.jwt.AuthenticationSuccessHandler;
import com.mlinyun.onlinecourse.basics.security.jwt.JwtTokenOncePerRequestFilter;
import com.mlinyun.onlinecourse.basics.security.jwt.SysAccessDeniedHandler;
import com.mlinyun.onlinecourse.basics.security.validate.ImageValidateFilter;
import com.mlinyun.onlinecourse.basics.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.config.Customizer.withDefaults;

@Schema(description = "Spring Security 配置类")
@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @Resource
    private SecurityUtil securityUtil;

    @Resource
    private SysLoginProperties sysLoginProperties;

    @Resource
    private NoAuthenticationProperties noAuthenticationProperties;

    @Resource
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Resource
    private AuthenticationFailHandler authenticationFailHandler;

    @Resource
    private SysAccessDeniedHandler sysAccessDeniedHandler;

    @Resource
    private ImageValidateFilter imageValidateFilter;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userDetailsService.loadUserByUsername(username);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtTokenOncePerRequestFilter authenticationJwtTokenFilter() throws Exception {
        return new JwtTokenOncePerRequestFilter(redisTemplateHelper, securityUtil, sysLoginProperties);
    }

    /**
     * 读取配置文件中不需要鉴权的 URL
     *
     * @param index 索引
     * @return 不需要鉴权的 URL
     */
    public String getNoAuthUrl(int index) {
        String url = noAuthenticationProperties.getAuthentication().get(index);
        return url.substring(1, url.length() - 1);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                // 指出这个 HttpSecurity 只适用于以 /** 开头的URL（即所有请求）
                .securityMatcher("/**")
                // 配置不需要身份认证的 URL（其他请求均需认证）
                .authorizeHttpRequests(authorize -> {
                            AtomicInteger index = new AtomicInteger();
                            noAuthenticationProperties.getAuthentication().forEach(url -> {
                                        authorize.requestMatchers(
                                                getNoAuthUrl(index.get())
                                        ).permitAll();
                                        index.getAndIncrement();
                                    }
                            );
                        }
                )
                // 开启表单登录
                .formLogin((formLogin) ->
                        formLogin
                                // 默认提示登录接口
                                .loginPage("/common/needLogin")
                                // 默认登录接口
                                .loginProcessingUrl("/user/login")
                                // 允许所有人访问
                                .permitAll()
                                // 登录成功
                                .successHandler(authenticationSuccessHandler)
                                // 登录失败
                                .failureHandler(authenticationFailHandler)
                )
                // 登出配置
                .logout(LogoutConfigurer::permitAll)
                // IFrame 跨域
                .headers(header -> {
                    header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
                })
                // 使用默认跨域配置
                .cors(withDefaults())
                // 关闭跨域拦截
                .csrf(AbstractHttpConfigurer::disable)
                // 配置 JWT
                .sessionManagement(sessionManagement -> {
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                // 系统菜单权限拦截
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling.accessDeniedHandler(sysAccessDeniedHandler);
                })
                // 验证码过滤
                .addFilterBefore(imageValidateFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}
