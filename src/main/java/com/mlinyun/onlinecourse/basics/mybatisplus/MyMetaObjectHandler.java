package com.mlinyun.onlinecourse.basics.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Schema(description = "MybatisPlus字段填充")
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    private static final String ANONYMOUS_USER = "anonymousUser";

    private static final String REDIS_PRE = "OAUSER:";

    private static final String CREATE_BY = "createBy";

    private static final String CREATE_TIME = "createTime";

    private static final String UPDATE_BY = "updateBy";

    private static final String UPDATE_TIME = "updateTime";

    private static final String DEFAULT_STR = "API接口";

    @Override
    @Operation(summary = "新增方法填充")
    public void insertFill(MetaObject metaObject) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!Objects.equals(ANONYMOUS_USER, principal.toString())) {
                UserDetails user = (UserDetails) principal;
                String str = redisTemplateHelper.get(REDIS_PRE + user.getUsername());
                if (str != null) {
                    this.setFieldValByName(CREATE_BY, str, metaObject);
                } else {
                    this.setFieldValByName(CREATE_BY, user.getUsername(), metaObject);
                }
            }
            this.setFieldValByName(CREATE_TIME, new Date(), metaObject);
        } catch (NullPointerException e) {
            this.setFieldValByName(CREATE_BY, DEFAULT_STR, metaObject);
            this.setFieldValByName(CREATE_TIME, new Date(), metaObject);
        }
    }

    @Override
    @Operation(summary = "编辑方法填充")
    public void updateFill(MetaObject metaObject) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!Objects.equals(ANONYMOUS_USER, principal.toString())) {
                UserDetails user = (UserDetails) principal;
                this.setFieldValByName(UPDATE_BY, user.getUsername(), metaObject);
                String str = redisTemplateHelper.get(REDIS_PRE + user.getUsername());
                if (str != null) {
                    this.setFieldValByName(UPDATE_BY, str, metaObject);
                } else {
                    this.setFieldValByName(UPDATE_BY, user.getUsername(), metaObject);
                }
            }
            this.setFieldValByName(UPDATE_TIME, new Date(), metaObject);
        } catch (NullPointerException e) {
            this.setFieldValByName(UPDATE_BY, DEFAULT_STR, metaObject);
            this.setFieldValByName(UPDATE_TIME, new Date(), metaObject);
        }
    }

}
