package com.mlinyun.onlinecourse.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mlinyun.onlinecourse.basics.baseClass.BaseEntity;
import com.mlinyun.onlinecourse.basics.constant.CommonConstant;
import com.mlinyun.onlinecourse.data.dto.PermissionDTO;
import com.mlinyun.onlinecourse.data.dto.RoleDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "user")
@TableName("user")
@Schema(description = "用户entity")
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "姓名")
    @NotNull(message = "姓名不能为空")
    @Size(max = 20, message = "姓名长度不能超过20")
    private String nickname;

    @Schema(description = "账号")
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]{4,16}$", message = "账号长度不合法")
    private String username;

    @Schema(description = "密码")
    @NotNull(message = "密码不能为空")
    private String password;

    @Schema(description = "密码的强度")
    @Column(length = 2)
    private String passStrength;

    @Schema(description = "手机号")
    @Pattern(regexp = "^[1][3,4,5,6,7,8,9][0-9]{9}$", message = "手机号格式错误")
    private String mobile;

    @Schema(description = "部门ID")
    private String departmentId;

    @Schema(description = "部门")
    private String departmentTitle;

    @Schema(description = "邮箱")
    @Pattern(regexp = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$", message = "邮箱格式错误")
    private String email;

    @Schema(description = "性别")
    private String sex;

    @Schema(description = "家庭地址A")
    private String address;

    @Schema(description = "家庭地址B")
    private String street;

    @Schema(description = "用户类型")
    private Integer type;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "启用状态")
    private Integer status = CommonConstant.USER_STATUS_NORMAL;

    @Schema(description = "头像")
    private String avatar = CommonConstant.USER_DEFAULT_AVATAR;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "生日")
    private Date birth;

    @Transient
    @TableField(exist = false)
    @Schema(description = "是否默认角色")
    private Integer defaultRole;

    @Transient
    @TableField(exist = false)
    @Schema(description = "用户拥有的菜单列表")
    private List<PermissionDTO> permissions;

    @Transient
    @TableField(exist = false)
    @Schema(description = "用户拥有的角色列表")
    private List<RoleDTO> roles;

}

