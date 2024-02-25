package com.mlinyun.onlinecourse.basics.security.permission;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mlinyun.onlinecourse.basics.constant.CommonConstant;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.data.entity.Permission;
import com.mlinyun.onlinecourse.data.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;

import java.util.*;

/**
 * 按钮权限过滤
 */
@Component
public class MySecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    @Resource
    private IPermissionService iPermissionService;

    @Resource
    private PathMatcher pathMatcher;

    private Map<String, Collection<ConfigAttribute>> map = null;

    private static final int INIT_MAP_SIZE = 16;

    @Operation(summary = "初始化按钮操作权限")
    public void loadResourceDefine() {
        map = new HashMap<>(INIT_MAP_SIZE);
        Collection<ConfigAttribute> configAttributeList;
        // 查询按钮操作权限
        QueryWrapper<Permission> perQw = new QueryWrapper<>();
        perQw.eq("type", CommonConstant.PERMISSION_OPERATION);
        perQw.eq("status", 0);
        perQw.orderByAsc("sortOrder");
        List<Permission> permissions = iPermissionService.list(perQw);
        for (Permission permission : permissions) {
            if (!NullUtils.isNull(permission.getTitle()) && !NullUtils.isNull(permission.getPath())) {
                configAttributeList = new ArrayList<>();
                configAttributeList.add(new SecurityConfig(permission.getTitle()));
                map.put(permission.getPath(), configAttributeList);
            }
        }
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }

    @Operation(summary = "URL请求权限过滤")
    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        if (map == null) {
            loadResourceDefine();
        }
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String resUrlStr = iterator.next();
            if (StrUtil.isNotBlank(resUrlStr) && pathMatcher.match(resUrlStr, ((FilterInvocation) o).getRequestUrl())) {
                return map.get(resUrlStr);
            }
        }
        return null;
    }

}
