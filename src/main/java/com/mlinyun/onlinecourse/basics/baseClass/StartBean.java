package com.mlinyun.onlinecourse.basics.baseClass;

import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.data.entity.User;
import com.mlinyun.onlinecourse.data.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Schema(description = "启动执行类")
public class StartBean implements ApplicationRunner {

    @Resource
    private IUserService iUserService;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    private static final String REDIS_USER_PRE = "USER:";

    @Override
    @Operation(summary = "启动执行方法", description = "用于日志记录用户姓名")
    public void run(ApplicationArguments args) {
        List<User> userList = iUserService.list();
        for (User user : userList) {
            if (user.getNickname() != null && user.getUsername() != null) {
                redisTemplateHelper.set(REDIS_USER_PRE + user.getUsername(), user.getNickname());
            }
        }
    }

}
