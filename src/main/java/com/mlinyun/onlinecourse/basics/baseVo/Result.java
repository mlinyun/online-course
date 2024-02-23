package com.mlinyun.onlinecourse.basics.baseVo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "统一返回数据VO类")
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "返回主数据", description = "泛型")
    private T result;

    @Schema(name = "是否请求成功", description = "true为请求成功，false为请求失败")
    private boolean success;

    @Schema(name = "返回状态代码", description = "默认200为成功")
    private Integer code;

    @Schema(name = "时间戳", description = "当前系统的时间戳")
    private long timestamp = System.currentTimeMillis();

    @Schema(name = "提示信息", description = "额外的提示信息")
    private String message;

}
