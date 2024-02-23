package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.dao.mapper.SettingMapper;
import com.mlinyun.onlinecourse.data.entity.Setting;
import com.mlinyun.onlinecourse.data.service.ISettingService;
import org.springframework.stereotype.Service;

/**
 * @author LingYun
 * @description 针对表【setting】的数据库操作Service实现
 * @createDate 2024-02-23 23:15:44
 */
@Service
public class ISettingServiceImpl extends ServiceImpl<SettingMapper, Setting>
        implements ISettingService {

}
