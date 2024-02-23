package com.mlinyun.onlinecourse.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mlinyun.onlinecourse.basics.baseClass.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "setting")
@TableName("setting")
@Schema(description = "配置entity")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Setting extends BaseEntity {

    @Serial
    private static final long serialVersionUID;

    static {
        serialVersionUID = 1L;
    }

    @Schema(description = "设置内容")
    private String value;

    public Setting(String id) {
        super.setId(id);
    }

}
