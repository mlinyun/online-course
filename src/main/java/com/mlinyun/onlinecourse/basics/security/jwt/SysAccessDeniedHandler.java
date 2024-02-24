package com.mlinyun.onlinecourse.basics.security.jwt;

import com.mlinyun.onlinecourse.basics.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;


@Schema(description = "自定义拒绝权限文案")
@Component
public class SysAccessDeniedHandler implements AccessDeniedHandler {

    private static final boolean RESPONSE_FAIL_FLAG = false;

    private static final int RESPONSE_NO_SELF_ROLE_CODE = 403;

    @Override
    @Schema(description = "重写自定义权限拒绝方法")
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) {
        ResponseUtil.out(response, ResponseUtil.resultMap(RESPONSE_FAIL_FLAG, RESPONSE_NO_SELF_ROLE_CODE, "您无权访问该菜单，谢谢！"));
    }

}
