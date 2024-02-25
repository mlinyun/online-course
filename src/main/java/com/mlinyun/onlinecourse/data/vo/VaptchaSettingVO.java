package com.mlinyun.onlinecourse.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class VaptchaSettingVO implements Serializable {

    @Schema(description = "键")
    private String secretKey;

    @Schema(description = "VID")
    private String vid;

    @Schema(description = "是否改变secretkey")
    private Boolean changed;

    @Schema(description = "场景")
    private String scene;

}
