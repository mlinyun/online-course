package com.mlinyun.onlinecourse.basics.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * RuntimeException 运行时异常处理类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "RuntimeException运行时异常处理类")
public class RuntimeExceptionHandler extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MSG = "系统出现错误";

    @Schema(description = "异常消息内容")
    private String msg;

    public RuntimeExceptionHandler() {
        super(DEFAULT_MSG);
        this.msg = DEFAULT_MSG;
    }

    public RuntimeExceptionHandler(String msg) {
        super(msg);
        this.msg = msg;
    }
}