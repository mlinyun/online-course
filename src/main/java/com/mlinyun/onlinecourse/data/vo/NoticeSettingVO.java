package com.mlinyun.onlinecourse.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class NoticeSettingVO implements Serializable {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "停留时长")
    private Integer duration;

    @Schema(description = "展示页面")
    private String position;

    @Schema(description = "公告状态")
    private Boolean open;

}
