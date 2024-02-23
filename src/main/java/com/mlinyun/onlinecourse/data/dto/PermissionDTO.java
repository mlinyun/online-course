package com.mlinyun.onlinecourse.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PermissionDTO {

    @Schema(description = "页面路径")
    private String path;

    @Schema(description = "菜单标题")
    private String title;

}
