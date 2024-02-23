package com.mlinyun.onlinecourse.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mlinyun.onlinecourse.basics.baseClass.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "role_permission")
@TableName("role_permission")
@Schema(description = "角色权限entity")
@EqualsAndHashCode(callSuper = false)
public class RolePermission extends BaseEntity {

    @Serial
    private static final long serialVersionUID;

    static {
        serialVersionUID = 1L;
    }

    @Schema(description = "权限ID")
    private String permissionId;

    @Schema(description = "角色ID")
    private String roleId;

}
