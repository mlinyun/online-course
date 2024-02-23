package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.dao.mapper.DepartmentHeaderMapper;
import com.mlinyun.onlinecourse.data.entity.DepartmentHeader;
import com.mlinyun.onlinecourse.data.service.IDepartmentHeaderService;
import org.springframework.stereotype.Service;

/**
 * @author LingYun
 * @description 针对表【department_header】的数据库操作Service实现
 * @createDate 2024-02-23 23:15:44
 */
@Service
public class IDepartmentHeaderServiceImpl extends ServiceImpl<DepartmentHeaderMapper, DepartmentHeader>
        implements IDepartmentHeaderService {

}
