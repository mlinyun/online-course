package com.mlinyun.onlinecourse.basics.security;


import com.mlinyun.onlinecourse.basics.constant.CommonConstant;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.data.dto.PermissionDTO;
import com.mlinyun.onlinecourse.data.dto.RoleDTO;
import com.mlinyun.onlinecourse.data.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


@Schema(description = "查询用户的角色和菜单权限")
public class SecurityUserDetails extends User implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<RoleDTO> roles;

    private List<PermissionDTO> permissions;

    @Override
    @Schema(description = "查询用户的角色和菜单权限")
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        // 菜单权限
        if (permissions != null && !permissions.isEmpty()) {
            for (PermissionDTO dto : permissions) {
                if (!NullUtils.isNull(dto.getTitle()) && !NullUtils.isNull(dto.getPath())) {
                    grantedAuthorityList.add(new SimpleGrantedAuthority(dto.getTitle()));
                }
            }
        }
        // 角色
        if (roles != null && !roles.isEmpty()) {
            roles.forEach(role -> {
                if (!NullUtils.isNull(role.getName())) {
                    grantedAuthorityList.add(new SimpleGrantedAuthority(role.getName()));
                }
            });
        }
        return grantedAuthorityList;
    }

    @Override
    @Schema(description = "账号是否启用")
    public boolean isEnabled() {
        return Objects.equals(CommonConstant.USER_STATUS_NORMAL, this.getStatus());
    }

    @Schema(description = "账号是否过期")
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Schema(description = "账号密码是否过期")
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Schema(description = "账号是否禁用")
    public boolean isAccountNonLocked() {
        return !Objects.equals(CommonConstant.USER_STATUS_LOCK, this.getStatus());
    }

    /**
     * 自定义类构造器
     *
     * @param user 系统账户
     */
    public SecurityUserDetails(User user) {
        if (user != null) {
            this.setUsername(user.getUsername());
            this.setPassword(user.getPassword());
            this.setStatus(user.getStatus());
            this.permissions = user.getPermissions();
            this.roles = user.getRoles();
        }
    }

}