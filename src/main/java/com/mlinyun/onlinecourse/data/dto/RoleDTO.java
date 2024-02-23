package com.mlinyun.onlinecourse.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoleDTO {

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色ID")
    private String id;

    @Schema(description = "角色备注")
    private String description;

}
