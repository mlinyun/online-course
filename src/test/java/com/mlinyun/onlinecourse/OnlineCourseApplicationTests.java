package com.mlinyun.onlinecourse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
class OnlineCourseApplicationTests {

    // 注入数据源对象
    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }

    // 测试 MySQL 数据源
    @Test
    public void datasourceTest() throws SQLException {
        // 获取数据源类型
        System.out.println("当前使用的数据源为：" + dataSource.getClass());
        // 获取数据库连接对象
        Connection connection = dataSource.getConnection();
        // 判断连接对象是否为空
        boolean flag = connection == null;
        System.out.println("数据库连接对象是否为空（true：为空，false：不为空）：" + flag);
        if (connection != null) {
            connection.close();
        }
    }

}
