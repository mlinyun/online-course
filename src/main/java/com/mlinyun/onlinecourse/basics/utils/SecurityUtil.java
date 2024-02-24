package com.mlinyun.onlinecourse.basics.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mlinyun.onlinecourse.basics.baseVo.TokenUser;
import com.mlinyun.onlinecourse.basics.exception.RuntimeExceptionHandler;
import com.mlinyun.onlinecourse.basics.parameter.SysLoginProperties;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.data.dto.PermissionDTO;
import com.mlinyun.onlinecourse.data.dto.RoleDTO;
import com.mlinyun.onlinecourse.data.entity.Permission;
import com.mlinyun.onlinecourse.data.entity.Role;
import com.mlinyun.onlinecourse.data.entity.User;
import com.mlinyun.onlinecourse.data.service.IPermissionService;
import com.mlinyun.onlinecourse.data.service.IRoleService;
import com.mlinyun.onlinecourse.data.service.IUserService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Schema(description = "鉴权工具类")
@Component
public class SecurityUtil {

    @Resource
    private SysLoginProperties tokenProperties;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @Resource
    private IUserService iUserService;

    @Resource
    private IRoleService iRoleService;

    @Resource
    private IPermissionService iPermissionService;

    private static final String TOKEN_REPLACE_FRONT_STR = "-";

    private static final String TOKEN_REPLACE_BACK_STR = "";

    private User selectByUserName(String title) {
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("username", title);
        User user = iUserService.getOne(userQw);
        if (user == null) {
            return null;
        }
        /**
         * 填充角色
         */
        QueryWrapper<Role> roleQw = new QueryWrapper<>();
        roleQw.inSql("id", "SELECT roleId FROM user_role WHERE delFlag = 0 AND userId = '" + user.getId() + "'");
        List<Role> roleList = iRoleService.list(roleQw);
        List<RoleDTO> roles = new ArrayList<>();
        for (Role role : roleList) {
            roles.add(new RoleDTO(role.getName(), role.getId(), role.getDescription()));
        }
        user.setRoles(roles);
        // 填充菜单
        QueryWrapper<Permission> permissionQw = new QueryWrapper<>();
        permissionQw.inSql("id", "SELECT roleId FROM role_permission WHERE delFlag = 0 AND permissionId = '" + user.getId() + "'");
        List<Permission> permissionList = iPermissionService.list(permissionQw);
        List<PermissionDTO> permissions = new ArrayList<>();
        for (Permission permission : permissionList) {
            if (!Objects.equals(1, permission.getType())) {
                permissions.add(new PermissionDTO(permission.getPath(), permission.getTitle()));
            }
        }
        user.setPermissions(permissions);
        return user;
    }

    @Schema(description = "获取新的用户Token")
    public String getToken(String username, Boolean saveLogin) {
        if (NullUtils.isNull(username)) {
            throw new RuntimeExceptionHandler("username不能为空");
        }
        boolean saved = false;
        if (saveLogin == null || saveLogin) {
            saved = true;
        }
        User selectUser = selectByUserName(username);
        // 菜单和角色的数组
        List<String> permissionTitleList = new ArrayList<>();
        if (tokenProperties.getSaveRoleFlag()) {
            for (PermissionDTO p : selectUser.getPermissions()) {
                if (!NullUtils.isNull(p.getTitle()) && !NullUtils.isNull(p.getPath())) {
                    permissionTitleList.add(p.getTitle());
                }
            }
            for (RoleDTO r : selectUser.getRoles()) {
                permissionTitleList.add(r.getName());
            }
        }
        String ansUserToken = UUID.randomUUID().toString().replace(TOKEN_REPLACE_FRONT_STR, TOKEN_REPLACE_BACK_STR);
        TokenUser tokenUser = new TokenUser(selectUser.getUsername(), permissionTitleList, saved);
        // 单点登录删除旧Token
        if (tokenProperties.getSsoFlag()) {
            String oldToken = redisTemplateHelper.get(SysLoginProperties.USER_TOKEN_PRE + selectUser.getUsername());
            if (StrUtil.isNotBlank(oldToken)) {
                redisTemplateHelper.delete(SysLoginProperties.HTTP_TOKEN_PRE + oldToken);
            }
        }
        // 保存至Redis备查
        if (saved) {
            redisTemplateHelper.set(SysLoginProperties.USER_TOKEN_PRE + selectUser.getUsername(), ansUserToken, tokenProperties.getUserSaveLoginTokenDays(), TimeUnit.DAYS);
            redisTemplateHelper.set(SysLoginProperties.HTTP_TOKEN_PRE + ansUserToken, JSON.toJSONString(tokenUser), tokenProperties.getUserSaveLoginTokenDays(), TimeUnit.DAYS);
        } else {
            redisTemplateHelper.set(SysLoginProperties.USER_TOKEN_PRE + selectUser.getUsername(), ansUserToken, tokenProperties.getUserTokenInvalidDays(), TimeUnit.MINUTES);
            redisTemplateHelper.set(SysLoginProperties.HTTP_TOKEN_PRE + ansUserToken, JSON.toJSONString(tokenUser), tokenProperties.getUserTokenInvalidDays(), TimeUnit.MINUTES);
        }
        return ansUserToken;
    }

    @Schema(description = "查询指定用户的权限列表")
    public List<GrantedAuthority> getCurrUserPerms(String userName) {
        List<GrantedAuthority> ans = new ArrayList<>();
        User selectUser = selectByUserName(userName);
        if (selectUser == null) {
            return ans;
        }
        List<PermissionDTO> perList = selectUser.getPermissions();
        if (perList.size() < 1) {
            return ans;
        }
        for (PermissionDTO vo : perList) {
            ans.add(new SimpleGrantedAuthority(vo.getTitle()));
        }
        return ans;
    }

    @Schema(description = "查询当前Token的用户对象")
    public User getCurrUser() {
        Object selectUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.equals("anonymousUser", selectUser.toString())) {
            throw new RuntimeExceptionHandler("登录失效");
        }
        UserDetails user = (UserDetails) selectUser;
        return selectByUserName(user.getUsername());
    }

}
