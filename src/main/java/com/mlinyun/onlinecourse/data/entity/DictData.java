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

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "dict_data")
@TableName("dict_data")
@Schema(description = "数据字典值entity")
@EqualsAndHashCode(callSuper = false)
public class DictData extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据字典ID")
    private String dictId;

    @Transient
    @TableField(exist = false)
    @Schema(description = "数据字典名称")
    private String dictName;

    @Schema(description = "数据字典键")
    private String title;

    @Schema(description = "数据字典值排序值")
    @Column(precision = 10, scale = 2)
    private BigDecimal sortOrder;

    @Schema(description = "数据字典值")
    private String value;

    @Schema(description = "数据字典值备注")
    private String description;

    @Schema(description = "是否启用")
    private Integer status = 0;

}
