package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.service.IRoleService;
import com.mlinyun.onlinecourse.data.entity.Role;
import com.mlinyun.onlinecourse.data.dao.mapper.RoleMapper;
import org.springframework.stereotype.Service;

/**
* @author LingYun
* @description 针对表【role】的数据库操作Service实现
* @createDate 2024-02-23 23:15:44
*/
@Service
public class IRoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements IRoleService {

}




