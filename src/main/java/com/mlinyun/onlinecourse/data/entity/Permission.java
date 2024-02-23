package com.mlinyun.onlinecourse.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mlinyun.onlinecourse.basics.baseClass.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "permission")
@TableName("permission")
@Schema(description = "菜单权限entity")
@EqualsAndHashCode(callSuper = false)
public class Permission extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单标题")
    private String title;

    @Schema(description = "菜单层级")
    private Integer level;

    @Schema(description = "启用状态")
    private Integer status = 0;

    @Schema(description = "菜单按钮类型")
    private Integer type;

    @Schema(description = "前端组件名称")
    private String component;

    @Schema(description = "页面路径")
    private String path;

    @Schema(description = "PC端图标")
    private String icon;

    @Schema(description = "父节点ID")
    private String parentId;

    @Schema(description = "按钮类型")
    private String buttonType;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "强制显示")
    private Boolean showAlways = true;

    @Schema(description = "菜单排序值")
    @Column(precision = 10, scale = 2)
    private BigDecimal sortOrder;

    @Transient
    @TableField(exist = false)
    @Schema(description = "节点展开状态")
    private Boolean expand = true;

    @Transient
    @TableField(exist = false)
    @Schema(description = "结点选中状态")
    private Boolean selected = false;

    @Transient
    @TableField(exist = false)
    @Schema(description = "结点勾选状态")
    private Boolean checked = false;

    @Transient
    @TableField(exist = false)
    @Schema(description = "子菜单列表")
    private List<Permission> children;

    @Transient
    @TableField(exist = false)
    @Schema(description = "菜单子权限列表")
    private List<String> permTypes;

}
