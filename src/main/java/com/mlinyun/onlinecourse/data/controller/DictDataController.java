package com.mlinyun.onlinecourse.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.PageUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.entity.Dict;
import com.mlinyun.onlinecourse.data.entity.DictData;
import com.mlinyun.onlinecourse.data.service.IDictDataService;
import com.mlinyun.onlinecourse.data.service.IDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Tag(name = "字典数据值接口")
@RequestMapping("/dictData")
@CacheConfig(cacheNames = "dictData")
@RestController
@Transactional
public class DictDataController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IDictService iDictService;

    @Resource
    private IDictDataService iDictDataService;

    private static final String REDIS_DIST_DATA_PRE_STR = "dictData::";

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询单个数据字典的值", logType = LogType.DATA_CENTER, doType = "DICT_DATA-01")
    @Operation(summary = "查询单个数据字典的值", description = "根据数据字典类型查询单个数据字典的值")
    @RequestMapping(value = "/getByType/{type}", method = RequestMethod.GET)
    public Result<Object> getByType(@PathVariable String type) {
        QueryWrapper<Dict> qw = new QueryWrapper<>();
        qw.eq("type", type);
        Dict selectDict = iDictService.getOne(qw);
        if (selectDict == null) {
            return ResultUtil.error("字典 " + type + " 不存在");
        }
        QueryWrapper<DictData> dataQw = new QueryWrapper<>();
        dataQw.eq("dictId", selectDict.getId());
        return ResultUtil.data(iDictDataService.list(dataQw));
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询数据字典值", logType = LogType.DATA_CENTER, doType = "DICT_DATA-02")
    @Operation(summary = "查询数据字典值", description = "查询数据字典值")
    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    public Result<IPage<DictData>> getByCondition(@ModelAttribute DictData dictData, @ModelAttribute PageVo page) {
        QueryWrapper<DictData> qw = new QueryWrapper<>();
        if (!NullUtils.isNull(dictData.getDictId())) {
            qw.eq("dictId", dictData.getDictId());
        }
        if (!Objects.equals(null, dictData.getStatus())) {
            qw.eq("status", dictData.getStatus());
        }
        if (!NullUtils.isNull(dictData.getTitle())) {
            qw.eq("title", dictData.getTitle());
        }
        return new ResultUtil<IPage<DictData>>().setData(iDictDataService.page(PageUtil.initMpPage(page), qw));
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "删除数据字典值", logType = LogType.DATA_CENTER, doType = "DICT_DATA-03")
    @Operation(summary = "删除数据字典值", description = "删除数据字典值")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result<Object> delByIds(@RequestParam String[] ids) {
        for (String dictDataId : ids) {
            DictData dictData = iDictDataService.getById(dictDataId);
            Dict dict = iDictService.getById(dictData.getDictId());
            iDictDataService.removeById(dictDataId);
            stringRedisTemplate.delete(REDIS_DIST_DATA_PRE_STR + dict.getType());
        }
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "添加数据字典值", logType = LogType.DATA_CENTER, doType = "DICT_DATA-04")
    @Operation(summary = "添加数据字典值", description = "添加数据字典值")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<Object> add(DictData dictData) {
        Dict selectDict = iDictService.getById(dictData.getDictId());
        if (selectDict == null) {
            return ResultUtil.error("字典不存在");
        }
        iDictDataService.saveOrUpdate(dictData);
        stringRedisTemplate.delete(REDIS_DIST_DATA_PRE_STR + selectDict.getType());
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "编辑数据字典值", logType = LogType.DATA_CENTER, doType = "DICT_DATA-05")
    @Operation(summary = "编辑数据字典值", description = "编辑数据字典值")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public Result<Object> edit(DictData dictData) {
        iDictDataService.saveOrUpdate(dictData);
        Dict selectDict = iDictService.getById(dictData.getDictId());
        stringRedisTemplate.delete(REDIS_DIST_DATA_PRE_STR + selectDict.getType());
        return ResultUtil.success();
    }

}
