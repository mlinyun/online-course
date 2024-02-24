package com.mlinyun.onlinecourse.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Schema(description = "菜单临时VO类")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDTO {

    @Schema(description = "页面路径")
    private String path;

    @Schema(description = "菜单标题")
    private String title;

}
