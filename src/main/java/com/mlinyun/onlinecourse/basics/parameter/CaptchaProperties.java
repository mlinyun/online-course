package com.mlinyun.onlinecourse.basics.parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * URL验证
 */
@Schema(description = "验证码接口配置")
@Data
@Configuration
@ConfigurationProperties(prefix = "intercept")
public class CaptchaProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "需要图片验证码验证的接口")
    private List<String> verification = new ArrayList<>();

    @Schema(description = "需要企微验证码验证的接口")
    private List<String> wechat = new ArrayList<>();

}
