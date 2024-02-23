package com.mlinyun.onlinecourse.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mlinyun.onlinecourse.basics.baseClass.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.util.List;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "role")
@TableName("role")
@Schema(description = "角色entity")
@EqualsAndHashCode(callSuper = false)
public class Role extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "数据权限")
    private int dataType;

    @Schema(description = "是否默认")
    private Boolean defaultRole;

    @Schema(description = "角色备注")
    private String description;

    @Transient
    @TableField(exist = false)
    @Schema(description = "角色拥有菜单列表")
    private List<RolePermission> permissions;

}
