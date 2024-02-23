package com.mlinyun.onlinecourse.basics.log;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Schema(description = "日志实体类")
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemLog {

    @Schema(description = "日志名称")
    public String logName() default "系统日志";

    @Schema(description = "日志类型")
    public LogType logType() default LogType.DEFAULT_OPERATION;

    @Schema(description = "操作代码")
    String doType() default "";

}
