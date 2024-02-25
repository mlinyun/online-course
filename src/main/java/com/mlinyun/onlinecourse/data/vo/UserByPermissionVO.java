package com.mlinyun.onlinecourse.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户菜单权限VO类")
@Data
public class UserByPermissionVO {

    private String userId;

    private String userName;

    private String roleStr;

    private String code;

    private String mobile;

}
