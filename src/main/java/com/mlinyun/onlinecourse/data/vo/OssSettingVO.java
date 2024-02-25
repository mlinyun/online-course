package com.mlinyun.onlinecourse.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class OssSettingVO implements Serializable {

    @Schema(description = "服务商")
    private String serviceName;

    @Schema(description = "域名")
    private String endpoint;

    @Schema(description = "http")
    private String http;

    @Schema(description = "本地存储路径")
    private String filePath;

}
