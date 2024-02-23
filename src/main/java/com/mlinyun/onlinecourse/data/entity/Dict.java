package com.mlinyun.onlinecourse.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mlinyun.onlinecourse.basics.baseClass.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "dict")
@TableName("dict")
@Schema(description = "数据字典entity")
@EqualsAndHashCode(callSuper = false)
public class Dict extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据字典标题")
    private String title;

    @Schema(description = "数据字典排序值")
    @Column(precision = 10, scale = 2)
    private BigDecimal sortOrder;

    @Schema(description = "数据字典备注")
    private String description;

    @Schema(description = "数据字典类型")
    private String type;

}
