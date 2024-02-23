package com.mlinyun.onlinecourse.basics.parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "release")
public class NoAuthenticationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "不鉴权的URL")
    private List<String> authentication = new ArrayList<>();

    @Schema(description = "不限流的URL")
    private List<String> limiting = new ArrayList<>();

}
