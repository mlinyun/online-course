package com.mlinyun.onlinecourse.basics.security.validate;

import com.mlinyun.onlinecourse.basics.parameter.CaptchaProperties;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 验证码过滤类
 */
@Schema(description = "验证码过滤类")
@Configuration
public class ImageValidateFilter extends OncePerRequestFilter {

    @Resource
    private CaptchaProperties captchaProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PathMatcher pathMatcher;

    private static final boolean RESPONSE_FAIL_FLAG = false;

    private static final int RESPONSE_CODE_FAIL_CODE = 500;

    @Override
    @Schema(description = "验证码过滤")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Boolean filterFlag = false;
        for (String requestURI : captchaProperties.getVerification()) {
            if (pathMatcher.match(requestURI, request.getRequestURI())) {
                filterFlag = true;
                break;
            }
        }
        if (!filterFlag) {
            filterChain.doFilter(request, response);
            return;
        }
        String verificationCodeId = request.getParameter("captchaId");
        String userInputCode = request.getParameter("code");
        if (NullUtils.isNull(userInputCode) || NullUtils.isNull(verificationCodeId)) {
            ResponseUtil.out(response, ResponseUtil.resultMap(RESPONSE_FAIL_FLAG, RESPONSE_CODE_FAIL_CODE, "验证码为空"));
            return;
        }
        String codeAnsInRedis = stringRedisTemplate.opsForValue().get(verificationCodeId);
        if (NullUtils.isNull(codeAnsInRedis)) {
            ResponseUtil.out(response, ResponseUtil.resultMap(RESPONSE_FAIL_FLAG, RESPONSE_CODE_FAIL_CODE, "已过期的验证码，需要重新填写"));
            return;
        }
        if (!Objects.equals(codeAnsInRedis.toLowerCase(), userInputCode.toLowerCase())) {
            ResponseUtil.out(response, ResponseUtil.resultMap(RESPONSE_FAIL_FLAG, RESPONSE_CODE_FAIL_CODE, "验证码不正确"));
            return;
        }
        stringRedisTemplate.delete(verificationCodeId);
        filterChain.doFilter(request, response);
    }

}
