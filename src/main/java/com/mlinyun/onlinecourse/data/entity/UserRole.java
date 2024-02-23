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
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;

@Data
@Accessors(chain = true)
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "user_role")
@TableName("user_role")
@Schema(description = "用户角色entity")
@EqualsAndHashCode(callSuper = false)
public class UserRole extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "用户ID")
    private String userId;

    @Transient
    @TableField(exist = false)
    @Schema(description = "用户名")
    private String userName;

    @Transient
    @TableField(exist = false)
    @Schema(description = "角色名")
    private String roleName;

}

