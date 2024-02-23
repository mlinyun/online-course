package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.service.ILogService;
import com.mlinyun.onlinecourse.data.entity.Log;
import com.mlinyun.onlinecourse.data.dao.mapper.LogMapper;
import org.springframework.stereotype.Service;

/**
* @author LingYun
* @description 针对表【log】的数据库操作Service实现
* @createDate 2024-02-23 23:15:44
*/
@Service
public class ILogServiceImpl extends ServiceImpl<LogMapper, Log>
    implements ILogService {

}




