package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.dao.mapper.UserRoleMapper;
import com.mlinyun.onlinecourse.data.entity.Role;
import com.mlinyun.onlinecourse.data.entity.UserRole;
import com.mlinyun.onlinecourse.data.service.IUserRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author LingYun
 * @description 针对表【user_role】的数据库操作Service实现
 * @createDate 2024-02-23 23:15:44
 */
@Service
public class IUserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public List<Role> findByUserId(String userId) {

        return userRoleMapper.findByUserId(userId);
    }

    @Override
    public List<String> findDepIdsByUserId(String userId) {

        return userRoleMapper.findDepIdsByUserId(userId);
    }

}
