package com.mlinyun.onlinecourse.basics.baseClass;

import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.utils.PageUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.List;

@Schema(description = "模板控制器层")
public abstract class BaseController<E, ID extends Serializable> {

    @Resource
    public abstract BaseService<E, ID> getInsService();

    @RequestMapping(value = "/getOne", name = "查询单个数据", method = RequestMethod.GET)
    @ResponseBody
    @Operation(description = "查询单个数据")
    public Result<E> getOne(@RequestParam ID id) {
        return new ResultUtil<E>().setData(getInsService().get(id));
    }

    @RequestMapping(value = "/getAll", name = "查询全部数据", method = RequestMethod.GET)
    @ResponseBody
    @Operation(description = "查询全部数据")
    public Result<List<E>> getAll() {
        return new ResultUtil<List<E>>().setData(getInsService().getAll());
    }

    @RequestMapping(value = "/getByPage", name = "查询数据", method = RequestMethod.GET)
    @ResponseBody
    @Operation(description = "查询数据")
    public Result<Page<E>> getByPage(PageVo page) {
        return new ResultUtil<Page<E>>().setData(getInsService().findAll(PageUtil.initPage(page)));
    }

    @RequestMapping(value = "/save", name = "新增数据", method = RequestMethod.POST)
    @ResponseBody
    @Operation(description = "新增数据")
    public Result<E> save(E entity) {
        return new ResultUtil<E>().setData(getInsService().save(entity));
    }

    @RequestMapping(value = "/update", name = "编辑数据", method = RequestMethod.PUT)
    @ResponseBody
    @Operation(description = "编辑数据")
    public Result<E> update(E entity) {
        return new ResultUtil<E>().setData(getInsService().update(entity));
    }

    @RequestMapping(value = "/count", name = "查询数据条数", method = RequestMethod.POST)
    @ResponseBody
    @Operation(description = "查询数据条数")
    public Result<Long> count() {
        return new ResultUtil<Long>().setData(getInsService().count());
    }

    @RequestMapping(value = "/delOne", name = "删除数据", method = RequestMethod.POST)
    @ResponseBody
    @Operation(description = "删除数据")
    public Result<Object> delByIds(@RequestParam ID id) {
        getInsService().delete(id);
        return new ResultUtil<Object>().setSuccessMsg("OK");
    }

    @RequestMapping(value = "/delByIds", name = "删除数据", method = RequestMethod.POST)
    @ResponseBody
    @Operation(description = "删除数据")
    public Result<Object> delByIds(@RequestParam ID[] ids) {
        for (ID id : ids) {
            getInsService().delete(id);
        }
        return new ResultUtil<Object>().setSuccessMsg("OK");
    }
}
