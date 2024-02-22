package com.mlinyun.onlinecourse.basics.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源自定义属性配置类
 */
@Configuration
public class DruidDataSourceConfig {
    /**
     * 通过 @Bean + @ConfigurationProperties(prefix = "spring.datasource") 注解的方式，
     * 将配置文件中的所有以 spring.datasource 开头的配置项都绑定到 DruidDataSource 对象中
     * 这样在 IOC 容器中获取到的 DataSource 对象就是通过开发者设置的属性构造的 DruidDataSource 对象了
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource() {
        return new DruidDataSource();
    }
}
