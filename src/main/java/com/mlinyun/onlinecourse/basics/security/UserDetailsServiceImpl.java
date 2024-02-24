package com.mlinyun.onlinecourse.basics.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.data.entity.User;
import com.mlinyun.onlinecourse.data.service.IUserService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Schema(description = "登录判断类")
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService iUserService;

    private static final String LOGIN_FAIL_DISABLED_PRE = "userLoginDisableFlag:";

    @Override
    @Schema(description = "根据账号/手机号查询用户所有信息")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String loginFailFlag = LOGIN_FAIL_DISABLED_PRE + username;
        String value = stringRedisTemplate.opsForValue().get(loginFailFlag);
        Long timeRest = stringRedisTemplate.getExpire(loginFailFlag, TimeUnit.MINUTES);
        if (!NullUtils.isNull(value)) {
            throw new UsernameNotFoundException("试错超限，请您在" + timeRest + "分钟后再登");
        }
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.and(wrapper -> wrapper.eq("username", username).or().eq("mobile", username));
        userQw.orderByDesc("create_time");
        userQw.last("limit 1");
        User superUser = iUserService.getOne(userQw);
        if (superUser == null) {
            throw new UsernameNotFoundException(username + "不存在");
        }
        return new SecurityUserDetails(superUser);
    }

}
