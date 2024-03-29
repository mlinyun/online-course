package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.dao.mapper.RolePermissionMapper;
import com.mlinyun.onlinecourse.data.entity.RolePermission;
import com.mlinyun.onlinecourse.data.service.IRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * @author LingYun
 * @description 针对表【role_permission】的数据库操作Service实现
 * @createDate 2024-02-23 23:15:44
 */
@Service
public class IRolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission>
        implements IRolePermissionService {

}
