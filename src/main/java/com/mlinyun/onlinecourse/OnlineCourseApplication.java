package com.mlinyun.onlinecourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineCourseApplication {

    public static void main(String[] args) {
        System.out.println("在线课程教学系统启动中......");
        SpringApplication.run(OnlineCourseApplication.class, args);
        System.out.println("在线课程教学系统启动成功！正在运行中......");
    }

}
