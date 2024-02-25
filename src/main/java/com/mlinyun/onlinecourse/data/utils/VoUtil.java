package com.mlinyun.onlinecourse.data.utils;

import cn.hutool.core.bean.BeanUtil;
import com.mlinyun.onlinecourse.data.entity.Permission;
import com.mlinyun.onlinecourse.data.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 菜单转换VO类
 */
public class VoUtil {

    @Operation(summary = "菜单转换VO类")
    public static MenuVO permissionToMenuVo(Permission permission) {
        MenuVO vo = new MenuVO();
        BeanUtil.copyProperties(permission, vo);
        return vo;
    }

}
