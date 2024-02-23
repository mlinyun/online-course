package com.mlinyun.onlinecourse.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
@Table(name = "file")
@TableName("file")
@Schema(description = "文件entity")
@EqualsAndHashCode(callSuper = false)
public class File extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "上传文件名")
    private String name;

    @Schema(description = "存储硬盘")
    private Integer location;

    @Schema(description = "文件存储路径")
    private String url;

    @Schema(description = "文件大小")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long size;

    @Schema(description = "实际文件名")
    private String fKey;

    @Schema(description = "文件类型")
    private String type;

    @Transient
    @TableField(exist = false)
    @Schema(description = "上传人")
    private String nickname;

}