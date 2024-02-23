package com.mlinyun.onlinecourse.data.service.impl;

import com.mlinyun.onlinecourse.data.dao.LogDao;
import com.mlinyun.onlinecourse.data.service.LogService;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * @author LingYun
 * @description 针对表【log】的数据库操作Service实现
 * @createDate 2024-02-23 23:15:44
 */
@Service
@Transactional
public class LogServiceImpl implements LogService {

    @Resource
    private LogDao logDao;

    @Override
    public LogDao getRepository() {
        return logDao;
    }
}
