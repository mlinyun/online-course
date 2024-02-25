package com.mlinyun.onlinecourse.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class SmsSettingVO implements Serializable {

    @Schema(description = "短信配置名称")
    private String serviceName;

    @Schema(description = "键KEY")
    private String secretKey;

    @Schema(description = "钥匙AK")
    private String accessKey;

    @Schema(description = "场景")
    private Integer type;

    @Schema(description = "签名内容")
    private String signName;

    @Schema(description = "是否改变secrectKey")
    private Boolean changed;

    @Schema(description = "模版code")
    private String templateCode;

}
