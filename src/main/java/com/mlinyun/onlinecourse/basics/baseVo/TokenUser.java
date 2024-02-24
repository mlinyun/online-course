package com.mlinyun.onlinecourse.basics.baseVo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户 Token VO类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户 Token VO类")
public class TokenUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "拥有的菜单权限")
    private List<String> permissions;

    @Schema(description = "是否自动登录")
    private Boolean saveLogin;

}
