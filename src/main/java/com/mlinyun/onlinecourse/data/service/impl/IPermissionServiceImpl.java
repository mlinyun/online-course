package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.service.IPermissionService;
import com.mlinyun.onlinecourse.data.entity.Permission;
import com.mlinyun.onlinecourse.data.dao.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

/**
* @author LingYun
* @description 针对表【permission】的数据库操作Service实现
* @createDate 2024-02-23 23:15:44
*/
@Service
public class IPermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission>
    implements IPermissionService {

}




