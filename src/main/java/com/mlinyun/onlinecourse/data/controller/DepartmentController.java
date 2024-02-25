package com.mlinyun.onlinecourse.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.constant.CommonConstant;
import com.mlinyun.onlinecourse.basics.exception.RuntimeExceptionHandler;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.basics.utils.CommonUtil;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.basics.utils.SecurityUtil;
import com.mlinyun.onlinecourse.data.entity.Department;
import com.mlinyun.onlinecourse.data.entity.DepartmentHeader;
import com.mlinyun.onlinecourse.data.entity.User;
import com.mlinyun.onlinecourse.data.service.IDepartmentHeaderService;
import com.mlinyun.onlinecourse.data.service.IDepartmentService;
import com.mlinyun.onlinecourse.data.service.IUserService;
import com.mlinyun.onlinecourse.data.utils.HibernateProxyTypeAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Tag(name = "部门管理接口")
@RequestMapping("/department")
@CacheConfig(cacheNames = "department")
@RestController
@Transactional
public class DepartmentController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @Resource
    private SecurityUtil securityUtil;

    @Resource
    private IDepartmentService iDepartmentService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IDepartmentHeaderService iDepartmentHeaderService;

    private static final String ONE_LEVEL_PARENT_TITLE = "一级部门";

    private static final String REDIS_DEPARTMENT_PRE_STR = "department::";

    private static final String REDIS_STEP_STR = ":";

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询子部门", logType = LogType.DATA_CENTER, doType = "DEP-01")
    @Operation(summary = "查询指定父部门下的子部门", description = "传递父部门ID查询该部门下的子部门，返回查询到的子部门列表")
    @RequestMapping(value = "/getByParentId/{parentId}", method = RequestMethod.GET)
    public Result<List<Department>> getByParentId(@PathVariable String parentId) {
        List<Department> list = null;
        User nowUser = securityUtil.getCurrUser();
        String key = REDIS_DEPARTMENT_PRE_STR + parentId + REDIS_STEP_STR + nowUser.getId();
        String value = stringRedisTemplate.opsForValue().get(key);
        // 如果缓存还在，优先加载缓存内的数据
        if (!NullUtils.isNull(value)) {
            list = new Gson().fromJson(value, new TypeToken<List<Department>>() {
            }.getType());
            return new ResultUtil<List<Department>>().setData(list);
        }
        QueryWrapper<Department> depQw = new QueryWrapper<>();
        depQw.eq("parentId", parentId);
        depQw.orderByAsc("sortOrder");
        list = iDepartmentService.list(depQw);
        list = setInfo(list);
        stringRedisTemplate.opsForValue().set(key, new GsonBuilder().registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY).create().toJson(list), 15L, TimeUnit.DAYS);
        return new ResultUtil<List<Department>>().setData(list);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "模糊搜索部门", logType = LogType.DATA_CENTER, doType = "DEP-02")
    @Operation(summary = "模糊搜索部门", description = "通过部门标题模糊搜索部门，返回匹配到的部门列表")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Result<List<Department>> search(@RequestParam String title) {
        QueryWrapper<Department> depQw = new QueryWrapper<>();
        depQw.like("title", title);
        depQw.orderByDesc("sortOrder");
        List<Department> departmentList = iDepartmentService.list(depQw);
        return new ResultUtil<List<Department>>().setData(setInfo(departmentList));
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "添加部门", logType = LogType.DATA_CENTER, doType = "DEP-03")
    @Operation(summary = "添加部门", description = "用于添加部门，返回添加结果")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<Object> add(Department department) {
        iDepartmentService.saveOrUpdate(department);
        if (!Objects.equals(CommonConstant.PARENT_ID, department.getParentId())) {
            Department parentDepartment = iDepartmentService.getById(department.getParentId());
            if (parentDepartment.getIsParent() == null || !parentDepartment.getIsParent()) {
                parentDepartment.setIsParent(true);
                iDepartmentService.saveOrUpdate(parentDepartment);
            }
        }
        Set<String> keyListInSet = redisTemplateHelper.keys(REDIS_DEPARTMENT_PRE_STR + "*");
        stringRedisTemplate.delete(keyListInSet);
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "编辑部门", logType = LogType.DATA_CENTER, doType = "DEP-04")
    @Operation(summary = "编辑部门", description = "用于编辑部门信息，返回编辑结果")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public Result<Object> edit(Department department, @RequestParam(required = false) String[] mainHeader, @RequestParam(required = false) String[] viceHeader) {
        Department oldDepartment = iDepartmentService.getById(department.getId());
        iDepartmentService.saveOrUpdate(department);
        QueryWrapper<DepartmentHeader> dhQw = new QueryWrapper<>();
        dhQw.eq("departmentId", department.getId());
        iDepartmentHeaderService.remove(dhQw);
        List<DepartmentHeader> departmentHeaderList = new ArrayList<>();
        if (mainHeader != null) {
            for (String mainHeaderId : mainHeader) {
                DepartmentHeader dh = new DepartmentHeader().setUserId(mainHeaderId).setDepartmentId(department.getId()).setType(0);
                departmentHeaderList.add(dh);
            }
        }
        if (viceHeader != null) {
            for (String viceHeaderId : viceHeader) {
                DepartmentHeader dh = new DepartmentHeader().setUserId(viceHeaderId).setDepartmentId(department.getId()).setType(1);
                departmentHeaderList.add(dh);
            }
        }
        iDepartmentHeaderService.saveOrUpdateBatch(departmentHeaderList);
        if (!oldDepartment.getTitle().equals(department.getTitle())) {
            QueryWrapper<User> userQw = new QueryWrapper<>();
            userQw.eq("departmentId", department.getId());
            List<User> userList = iUserService.list(userQw);
            for (User user : userList) {
                user.setDepartmentTitle(department.getTitle());
                iUserService.saveOrUpdate(user);
            }
            Set<String> keysUser = redisTemplateHelper.keys("user:" + "*");
            stringRedisTemplate.delete(keysUser);
        }
        Set<String> keys = redisTemplateHelper.keys("department:" + "*");
        if (keys != null) {
            stringRedisTemplate.delete(keys);
        }
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "批量删除部门", logType = LogType.DATA_CENTER, doType = "DEP-05")
    @Operation(summary = "批量删除部门", description = "通过传递的部门ID列表批量删除部门，返回删除结果")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result<Object> delByIds(@RequestParam String[] ids) {
        for (String departmentId : ids) {
            deleteRecursion(departmentId, ids);
        }
        Set<String> keyListInSet = redisTemplateHelper.keys("department:" + "*");
        if (keyListInSet != null) {
            stringRedisTemplate.delete(keyListInSet);
        }
        Set<String> keysUserRoleData = redisTemplateHelper.keys("userRole::depIds:" + "*");
        stringRedisTemplate.delete(keysUserRoleData);
        return ResultUtil.success();
    }

    @Operation(summary = "迭代删除部门")
    public void deleteRecursion(String id, String[] ids) {
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("departmentId", id);
        long userCountInDepartment = iUserService.count(userQw);
        if (userCountInDepartment > 0L) {
            throw new RuntimeExceptionHandler("不能删除包含员工的部门");
        }
        Department department = iDepartmentService.getById(id);
        Department parentDepartment = null;
        if (department != null && !NullUtils.isNull(department.getParentId())) {
            parentDepartment = iDepartmentService.getById(department.getParentId());
        }
        iDepartmentService.removeById(id);
        QueryWrapper<DepartmentHeader> dhQw = new QueryWrapper<>();
        dhQw.eq("departmentId", id);
        iDepartmentHeaderService.remove(dhQw);
        if (parentDepartment != null) {
            QueryWrapper<Department> depQw = new QueryWrapper<>();
            depQw.eq("parentId", parentDepartment.getId());
            depQw.orderByAsc("sortOrder");
            List<Department> childrenDepartmentList = iDepartmentService.list(depQw);
            if (childrenDepartmentList == null || childrenDepartmentList.size() < 1) {
                parentDepartment.setIsParent(false);
                iDepartmentService.saveOrUpdate(parentDepartment);
            }
        }
        QueryWrapper<Department> depQw = new QueryWrapper<>();
        depQw.eq("parentId", id);
        depQw.orderByAsc("sortOrder");
        List<Department> departmentList = iDepartmentService.list(depQw);
        for (Department judgeDepartment : departmentList) {
            if (!CommonUtil.judgeIds(judgeDepartment.getId(), ids)) {
                deleteRecursion(judgeDepartment.getId(), ids);
            }
        }
    }

    @Operation(summary = "增加一级部门标识")
    public List<Department> setInfo(List<Department> list) {
        list.forEach(item -> {
            if (!Objects.equals(CommonConstant.PARENT_ID, item.getParentId())) {
                Department parentDepartment = iDepartmentService.getById(item.getParentId());
                if (parentDepartment == null) {
                    item.setParentTitle("无");
                } else {
                    item.setParentTitle(parentDepartment.getTitle());
                }
            } else {
                item.setParentTitle(ONE_LEVEL_PARENT_TITLE);
            }
            QueryWrapper<DepartmentHeader> dh1 = new QueryWrapper<>();
            dh1.eq("departmentId", item.getId());
            dh1.eq("type", 0);
            List<DepartmentHeader> headerList1 = iDepartmentHeaderService.list(dh1);
            List<String> mainHeaderList = new ArrayList<>();
            for (DepartmentHeader dh : headerList1) {
                mainHeaderList.add(dh.getUserId());
            }
            item.setMainHeader(mainHeaderList);

            QueryWrapper<DepartmentHeader> dh2 = new QueryWrapper<>();
            dh2.eq("departmentId", item.getId());
            dh2.eq("type", 1);
            List<DepartmentHeader> headerList2 = iDepartmentHeaderService.list(dh2);
            List<String> viceHeaderList = new ArrayList<>();
            for (DepartmentHeader dh : headerList2) {
                viceHeaderList.add(dh.getUserId());
            }
            item.setViceHeader(viceHeaderList);
        });
        return list;
    }

    @Operation(summary = "添加模拟搜索标志")
    private String addLikeStr(String str) {
        return "%" + str + "%";
    }

}
