package com.mlinyun.onlinecourse.basics.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义加密器
 */
@Configuration
public class MyJasyptConfig {

    // Jasypt 加密密钥
    private static final String key = "online-course";

    // 该方法用于获取加密之后的密码
    public static void main(String[] args) {
        PooledPBEStringEncryptor encryptor = getPooledPBEStringEncryptor();

        String mysqlUrl = encryptor.encrypt("jdbc:mysql://localhost:3306/online_course?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true");
        String mysqlUsername = encryptor.encrypt("root");
        String mysqlPassword = encryptor.encrypt("123456");
        String redisPassword = encryptor.encrypt("123456");
        System.out.println("mysqlUrl " + mysqlUrl);
        System.out.println("mysqlUsername " + mysqlUsername);
        System.out.println("mysqlPassword " + mysqlPassword);
        System.out.println("redisPassword " + redisPassword);
    }

    private static PooledPBEStringEncryptor getPooledPBEStringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(MyJasyptConfig.key);
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

    @Bean(name = "CodeEncryptorBean")
    public StringEncryptor CodeEncryptorBean() {
        return getPooledPBEStringEncryptor();
    }
}
