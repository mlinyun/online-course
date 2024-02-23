package com.mlinyun.onlinecourse.basics.baseVo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分页VO类")
public class PageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "排序名称", description = "排序的字段名")
    private String sort;

    @Schema(name = "页码编号", description = "即展示第几页")
    private int pageNumber;

    @Schema(name = "排序类型", description = "升序asc,降序desc")
    private String order;

    @Schema(name = "每页个数", description = "建议设置为15")
    private int pageSize;

}
