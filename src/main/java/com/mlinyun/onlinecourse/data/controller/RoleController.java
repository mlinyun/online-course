package com.mlinyun.onlinecourse.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.PageUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.entity.Role;
import com.mlinyun.onlinecourse.data.entity.RolePermission;
import com.mlinyun.onlinecourse.data.entity.UserRole;
import com.mlinyun.onlinecourse.data.service.IRolePermissionService;
import com.mlinyun.onlinecourse.data.service.IRoleService;
import com.mlinyun.onlinecourse.data.service.IUserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Tag(name = "角色管理接口")
@RequestMapping("/role")
@CacheConfig(cacheNames = "role")
@RestController
@Transactional
public class RoleController {

    @Resource
    private IRoleService iRoleService;

    @Resource
    private IUserRoleService iUserRoleService;

    @Resource
    private IRolePermissionService iRolePermissionService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询全部角色", logType = LogType.DATA_CENTER, doType = "ROLE-01")
    @Operation(summary = "查询全部角色", description = "查询全部角色")
    @RequestMapping(value = "/getAllList", method = RequestMethod.GET)
    public Result<Object> getAllList() {
        return ResultUtil.data(iRoleService.list());
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "分页查询角色", logType = LogType.DATA_CENTER, doType = "ROLE-02")
    @Operation(summary = "分页查询角色", description = "分页查询角色")
    @RequestMapping(value = "/getAllByPage", method = RequestMethod.GET)
    public Result<IPage<Role>> getRoleByPage(@ModelAttribute Role role, @ModelAttribute PageVo page) {
        QueryWrapper<Role> qw = new QueryWrapper<>();
        if (!NullUtils.isNull(role.getName())) {
            qw.like("name", role.getName());
        }
        if (!NullUtils.isNull(role.getDescription())) {
            qw.like("description", role.getDescription());
        }
        IPage<Role> roleList = iRoleService.page(PageUtil.initMpPage(page));
        for (Role r : roleList.getRecords()) {
            QueryWrapper<RolePermission> rpQw = new QueryWrapper<>();
            rpQw.eq("roleId", r.getId());
            r.setPermissions(iRolePermissionService.list(rpQw));
        }
        return new ResultUtil<IPage<Role>>().setData(roleList);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "配置默认角色", logType = LogType.DATA_CENTER, doType = "ROLE-03")
    @Operation(summary = "配置默认角色", description = "配置默认角色")
    @RequestMapping(value = "/setDefault", method = RequestMethod.POST)
    public Result<Object> setDefault(@RequestParam String id, @RequestParam Boolean isDefault) {
        Role role = iRoleService.getById(id);
        if (role == null) {
            return ResultUtil.error("角色已被删除");
        }
        role.setDefaultRole(isDefault);
        iRoleService.saveOrUpdate(role);
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "修改菜单权限", logType = LogType.DATA_CENTER, doType = "ROLE-04")
    @Operation(summary = "修改菜单权限", description = "修改菜单权限")
    @RequestMapping(value = "/editRolePerm", method = RequestMethod.POST)
    public Result<Object> editRolePerm(@RequestParam String roleId, @RequestParam(required = false) String[] permIds) {
        Role role = iRoleService.getById(roleId);
        if (role == null) {
            return ResultUtil.error("角色已被删除");
        }
        if (permIds == null) {
            permIds = new String[0];
        }
        QueryWrapper<RolePermission> oldQw = new QueryWrapper<>();
        oldQw.eq("roleId", role.getId());
        List<RolePermission> oldPermissionList = iRolePermissionService.list(oldQw);
        // 判断新增 = oldPermissionList没有 permIds有
        for (String permId : permIds) {
            boolean flag = true;
            for (RolePermission rp : oldPermissionList) {
                if (Objects.equals(permId, rp.getPermissionId())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permId);
                iRolePermissionService.saveOrUpdate(rp);
            }
        }
        // 判断删除 = oldPermissionList有 permIds没有
        for (RolePermission rp : oldPermissionList) {
            boolean flag = true;
            for (String permId : permIds) {
                if (Objects.equals(permId, rp.getPermissionId())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                iRolePermissionService.removeById(rp.getId());
            }
        }
        Set<String> keysUser = redisTemplateHelper.keys("user:" + "*");
        stringRedisTemplate.delete(keysUser);
        Set<String> keysUserRole = redisTemplateHelper.keys("userRole:" + "*");
        stringRedisTemplate.delete(keysUserRole);
        Set<String> keysUserMenu = redisTemplateHelper.keys("permission::userMenuList:*");
        stringRedisTemplate.delete(keysUserMenu);
        return ResultUtil.data();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "修改角色数据权限", logType = LogType.DATA_CENTER, doType = "ROLE-05")
    @Operation(summary = "修改角色数据权限", description = "修改角色数据权限")
    @RequestMapping(value = "/editRoleDep", method = RequestMethod.POST)
    public Result<Object> editRoleDep(@RequestParam String roleId, @RequestParam Integer dataType, @RequestParam(required = false) String[] depIds) {
        Role role = iRoleService.getById(roleId);
        role.setDataType(dataType);
        iRoleService.saveOrUpdate(role);
        Set<String> keys = redisTemplateHelper.keys("department:" + "*");
        stringRedisTemplate.delete(keys);
        Set<String> keysUserRole = redisTemplateHelper.keys("userRole:" + "*");
        stringRedisTemplate.delete(keysUserRole);
        return ResultUtil.data();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "新增角色", logType = LogType.DATA_CENTER, doType = "ROLE-06")
    @Operation(summary = "新增角色", description = "新增角色")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Result<Role> save(Role role) {
        iRoleService.saveOrUpdate(role);
        return new ResultUtil<Role>().setData(role);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "编辑角色", logType = LogType.DATA_CENTER, doType = "ROLE-07")
    @Operation(summary = "编辑角色", description = "编辑角色")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public Result<Role> edit(Role role) {
        iRoleService.saveOrUpdate(role);
        Set<String> keysUser = redisTemplateHelper.keys("user:" + "*");
        stringRedisTemplate.delete(keysUser);
        Set<String> keysUserRole = redisTemplateHelper.keys("userRole:" + "*");
        stringRedisTemplate.delete(keysUserRole);
        return new ResultUtil<Role>().setData(role);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "删除角色", logType = LogType.DATA_CENTER, doType = "ROLE-08")
    @Operation(summary = "删除角色", description = "删除角色")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result<Object> delByIds(@RequestParam String[] ids) {
        for (String id : ids) {
            QueryWrapper<UserRole> urQw = new QueryWrapper<>();
            urQw.eq("roleId", id);
            long urCount = iUserRoleService.count(urQw);
            if (urCount > 0L) {
                return ResultUtil.error("不能删除正在使用的角色");
            }
        }
        for (String id : ids) {
            iRoleService.removeById(id);
            QueryWrapper<RolePermission> rpQw = new QueryWrapper<>();
            rpQw.eq("roleId", id);
            iRolePermissionService.remove(rpQw);
        }
        return ResultUtil.success();
    }

}
