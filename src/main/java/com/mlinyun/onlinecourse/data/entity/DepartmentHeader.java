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
@Table(name = "department_header")
@TableName("department_header")
@Schema(description = "部门负责人entity")
@EqualsAndHashCode(callSuper = false)
public class DepartmentHeader extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "部门ID")
    private String departmentId;

    @Schema(description = "领导类型")
    private Integer type = 0;

}
