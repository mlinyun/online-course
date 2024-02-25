package com.mlinyun.onlinecourse.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MenuVO {

    @Schema(description = "菜单标题")
    private String title;

    @Schema(description = "菜单ID")
    private String id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "父菜单ID")
    private String parentId;

    @Schema(description = "菜单层级")
    private Integer level;

    @Schema(description = "是否强制显示")
    private Boolean showAlways;

    @Schema(name = "菜单类型", description = "1具体操作 -1顶部菜单 0页面")
    private Integer type;

    @Schema(description = "组件")
    private String component;

    @Schema(description = "页面路径")
    private String path;

    @Schema(description = "PC端图标")
    private String icon;

    @Schema(description = "按钮类型")
    private String buttonType;

    @Schema(description = "网页链接")
    private String url;

    @Schema(description = "子权限列表")
    private List<String> permTypes;

    @Schema(description = "子菜单列表")
    private List<MenuVO> children;

}
