package com.mlinyun.onlinecourse.data.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置信息获取
 */
@Tag(name = "数据源信息", description = "该接口为数据源接口，主要用来查看后端数据源的信息")
@RestController
@RequestMapping("/datasource")
public class DataSourceController {

    // 自动配置，因此可以直接通过 @Autowired 注入进来
    @Resource
    private DataSource dataSource;

    /**
     * 查询数据源信息1
     *
     * @return 数据库连接对象
     * @throws SQLException SQL 异常
     */
    @ApiOperationSupport(author = "LingYun")
    @Operation(summary = "查询数据源信息1", description = "获取数据库连接对象，判断数据连接对象是否为空")
    @GetMapping("/info1")
    public Map<String, String> datasourceInfo1() throws SQLException {
        Map<String, String> result = new HashMap<>();
        result.put("数据源类名", dataSource.getClass() + "");
        // 获取数据库连接对象
        Connection connection = dataSource.getConnection();
        // 判断连接对象是否为空
        result.put("能否正确获得连接", String.valueOf(connection != null));
        if (connection != null) {
            connection.close();
        }
        return result;
    }

    /**
     * 查询数据源信息2
     *
     * @return 数据库连接对象的具体信息
     * @throws SQLException SQL 异常
     */
    @ApiOperationSupport(author = "LingYun")
    @Operation(summary = "查询数据源信息2", description = "获取数据库连接对象的具体信息")
    @GetMapping("/info2")
    public Map<String, Object> datasourceInfo2() throws SQLException {
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        Map<String, Object> result = new HashMap<>();
        result.put("数据源类名", druidDataSource.getClass() + "");
        // 获取数据库连接对象
        Connection connection = druidDataSource.getConnection();
        // 判断连接对象是否为空
        result.put("能否正确获得连接", connection != null);
        result.put("initialSize值为", druidDataSource.getInitialSize());
        result.put("maxActive值为", druidDataSource.getMaxActive());
        result.put("minIdle值为", druidDataSource.getMinIdle());
        result.put("validationQuery值为", druidDataSource.getValidationQuery());
        result.put("maxWait值为", druidDataSource.getMaxWait());
        if (connection != null) {
            connection.close();
        }
        return result;
    }
}
