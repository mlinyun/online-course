package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.service.IDepartmentService;
import com.mlinyun.onlinecourse.data.entity.Department;
import com.mlinyun.onlinecourse.data.dao.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;

/**
* @author LingYun
* @description 针对表【department】的数据库操作Service实现
* @createDate 2024-02-23 23:15:44
*/
@Service
public class IDepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
    implements IDepartmentService {

}




