package com.mlinyun.onlinecourse.basics.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

@Schema(description = "自定义异常")
public class SysAuthException extends InternalAuthenticationServiceException {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MSG = "系统鉴权失败";

    @Schema(description = "异常消息内容")
    private String msg;

    public SysAuthException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public SysAuthException() {
        super(DEFAULT_MSG);
        this.msg = DEFAULT_MSG;
    }

    public SysAuthException(String msg, Throwable t) {
        super(msg, t);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
