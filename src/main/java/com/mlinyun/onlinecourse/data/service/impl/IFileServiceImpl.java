package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.dao.mapper.FileMapper;
import com.mlinyun.onlinecourse.data.entity.File;
import com.mlinyun.onlinecourse.data.service.IFileService;
import org.springframework.stereotype.Service;

/**
 * @author LingYun
 * @description 针对表【file】的数据库操作Service实现
 * @createDate 2024-02-23 23:15:44
 */
@Service
public class IFileServiceImpl extends ServiceImpl<FileMapper, File>
        implements IFileService {

}
