package com.mlinyun.onlinecourse.data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mlinyun.onlinecourse.data.entity.Role;
import com.mlinyun.onlinecourse.data.entity.UserRole;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * @author LingYun
 * @description 针对表【user_role】的数据库操作Service
 * @createDate 2024-02-23 23:15:44
 */
@CacheConfig(cacheNames = "userRole")
public interface IUserRoleService extends IService<UserRole> {

    /**
     * 通过用户id获取
     *
     * @param userId
     * @return
     */
    @Cacheable(key = "#userId")
    List<Role> findByUserId(String userId);

    /**
     * 通过用户id获取用户角色关联的部门数据
     *
     * @param userId
     * @return
     */
    List<String> findDepIdsByUserId(String userId);

}
