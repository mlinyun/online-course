package com.mlinyun.onlinecourse.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mlinyun.onlinecourse.data.service.IDictService;
import com.mlinyun.onlinecourse.data.entity.Dict;
import com.mlinyun.onlinecourse.data.dao.mapper.DictMapper;
import org.springframework.stereotype.Service;

/**
* @author LingYun
* @description 针对表【dict】的数据库操作Service实现
* @createDate 2024-02-23 23:15:44
*/
@Service
public class IDictServiceImpl extends ServiceImpl<DictMapper, Dict>
    implements IDictService {

}




