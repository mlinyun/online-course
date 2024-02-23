package com.mlinyun.onlinecourse.data.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.gson.Gson;
import com.mlinyun.onlinecourse.basics.baseClass.BaseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "log")
@TableName("log")
@Schema(description = "日志entity")
@EqualsAndHashCode(callSuper = false)
public class Log extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "日志标题")
    private String name;

    @Schema(description = "日志类型")
    private Integer logType;

    @Schema(description = "日志代码")
    private String code;

    @Schema(description = "设备")
    private String device;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求方式")
    private String requestType;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "参数")
    private String requestParam;

    @Schema(description = "触发者")
    private String username;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "IP定位")
    private String ipInfo;

    @Schema(description = "消耗毫秒数")
    private Integer costTime;

    @Transient
    @TableField(exist = false)
    @Schema(description = "搜索开始时间")
    private String startDate;

    @Transient
    @TableField(exist = false)
    @Schema(description = "搜索结束时间")
    private String endDate;

    @Operation(description = "MAP转换为字符串")
    public static String mapToString(Map<String, String[]> paramMap) {
        if (paramMap == null) {
            return "";
        }
        Map<String, Object> params = new HashMap<>(16);
        for (Map.Entry<String, String[]> keyInMap : paramMap.entrySet()) {
            String keyItemInMap = keyInMap.getKey();
            String paramValue = (keyInMap.getValue() != null && keyInMap.getValue().length > 0 ? keyInMap.getValue()[0] : "");
            String objStr = StrUtil.endWithIgnoreCase(keyInMap.getKey(), "password") ? "密码隐藏" : paramValue;
            params.put(keyItemInMap, objStr);
        }
        return new Gson().toJson(params);
    }

    @Operation(description = "Map转换为JSON数据")
    public void setMapToParams(Map<String, String[]> paramMap) {
        this.requestParam = mapToString(paramMap);
    }

}

