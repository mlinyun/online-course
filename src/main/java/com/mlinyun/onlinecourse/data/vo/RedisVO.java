package com.mlinyun.onlinecourse.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RedisVO {

    @Schema(description = "Redis键")
    private String key;

    @Schema(description = "Redis值")
    private String value;

    @Schema(description = "保存秒数")
    private Long expireTime;

}
