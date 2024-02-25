package com.mlinyun.onlinecourse.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Tag(name = "字典管理接口")
@RequestMapping("/dict")
@CacheConfig(cacheNames = "dict")
@RestController
@Transactional
public class DictController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IDictService iDictService;

    @Resource
    private IDictDataService iDictDataService;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询所有数据字典", logType = LogType.DATA_CENTER, doType = "DICT-01")
    @Operation(summary = "查询所有数据字典", description = "查询所有数据字典，返回查询到的数据字典列表")
    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public Result<List<Dict>> getAll() {
        return new ResultUtil<List<Dict>>().setData(iDictService.list());
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "模拟搜索数据字典", logType = LogType.DATA_CENTER, doType = "DICT-02")
    @Operation(summary = "模拟搜索数据字典", description = "根据数据字典标题进行模拟搜索数据字典，返回匹配到的数据字典列表")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Result<List<Dict>> search(@RequestParam String key) {
        QueryWrapper<Dict> qw = new QueryWrapper<>();
        qw.like("title", key);
        return new ResultUtil<List<Dict>>().setData(iDictService.list(qw));
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "添加数据字典", logType = LogType.DATA_CENTER, doType = "DICT-03")
    @Operation(summary = "添加数据字典", description = "用于添加数据字典，返回添加结果")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<Object> add(Dict dict) {
        QueryWrapper<Dict> qw = new QueryWrapper<>();
        qw.eq("type", dict.getType());
        long dictCount = iDictService.count(qw);
        if (dictCount < 1L) {
            iDictService.saveOrUpdate(dict);
            return ResultUtil.success();
        }
        return ResultUtil.error("字典已存在,不能同名");
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "编辑数据字典", logType = LogType.DATA_CENTER, doType = "DICT-04")
    @Operation(summary = "编辑数据字典", description = "用于更新数据字典信息，返回更新结果")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public Result<Object> edit(Dict dict) {
        Dict oldDict = iDictService.getById(dict.getId());
        if (oldDict == null) {
            return ResultUtil.error("字典已被删除");
        }
        if (!Objects.equals(dict.getType(), oldDict.getType())) {
            QueryWrapper<Dict> qw = new QueryWrapper<>();
            qw.eq("type", dict.getType());
            long dictCount = iDictService.count(qw);
            if (dictCount > 0L) {
                return ResultUtil.error("字典已存在,不能同名");
            }
        }
        iDictService.saveOrUpdate(dict);
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "批量删除数据字典", logType = LogType.DATA_CENTER, doType = "DICT-05")
    @Operation(summary = "批量删除数据字典", description = "通过传递的数据字典ID列表批量删除数据字典，返回删除结果")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result<Object> delByIds(@RequestParam String[] ids) {
        for (String distId : ids) {
            Dict selectDict = iDictService.getById(distId);
            if (selectDict == null) {
                continue;
            }
            iDictService.removeById(distId);
            QueryWrapper<DictData> qw = new QueryWrapper<>();
            qw.eq("dictId", distId);
            iDictDataService.remove(qw);
            stringRedisTemplate.delete("dictData::" + selectDict.getType());
        }
        return ResultUtil.success();
    }

}
