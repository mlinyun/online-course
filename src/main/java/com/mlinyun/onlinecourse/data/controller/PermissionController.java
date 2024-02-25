package com.mlinyun.onlinecourse.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.constant.CommonConstant;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.basics.security.permission.MySecurityMetadataSource;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.basics.utils.SecurityUtil;
import com.mlinyun.onlinecourse.data.entity.*;
import com.mlinyun.onlinecourse.data.service.*;
import com.mlinyun.onlinecourse.data.utils.VoUtil;
import com.mlinyun.onlinecourse.data.vo.MenuVO;
import com.mlinyun.onlinecourse.data.vo.UserByPermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Tag(name = "菜单/权限管理接口")
@RequestMapping("/permission")
@CacheConfig(cacheNames = "permission")
@RestController
@Transactional
public class PermissionController {

    @Resource
    private SecurityUtil securityUtil;

    @Resource
    private MySecurityMetadataSource mySecurityMetadataSource;

    @Resource
    private IRoleService iRoleService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IRolePermissionService iRolePermissionService;

    @Resource
    private IPermissionService iPermissionService;

    @Resource
    private IUserRoleService iUserRoleService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询菜单权限拥有者", logType = LogType.DATA_CENTER, doType = "PERMISSION-01")
    @Operation(summary = "查询菜单权限拥有者", description = "查询菜单权限拥有者")
    @RequestMapping(value = "/getUserByPermission", method = RequestMethod.GET)
    public Result<List<UserByPermissionVO>> getUserByPermission(@RequestParam String permissionId) {
        Permission permission = iPermissionService.getById(permissionId);
        if (permission == null) {
            return ResultUtil.error("该菜单已被删除");
        }
        List<UserByPermissionVO> ansList = new ArrayList<>();
        // 查询用户
        QueryWrapper<RolePermission> qw = new QueryWrapper<>();
        qw.eq("permissionId", permissionId);
        List<RolePermission> rolePermissionList = iRolePermissionService.list(qw);
        for (RolePermission rp : rolePermissionList) {
            Role role = iRoleService.getById(rp.getRoleId());
            if (role != null) {
                QueryWrapper<UserRole> urQw = new QueryWrapper<>();
                urQw.eq("roleId", role.getId());
                List<UserRole> userRoleList = iUserRoleService.list(urQw);
                for (UserRole ur : userRoleList) {
                    User user = iUserService.getById(ur.getUserId());
                    if (user != null) {
                        boolean flag = false;
                        for (UserByPermissionVO vo : ansList) {
                            if (Objects.equals(vo.getUserId(), user.getId())) {
                                flag = true;
                                vo.setRoleStr(vo.getRoleStr() + role.getName() + "(" + role.getDescription() + ") ");
                                break;
                            }
                        }
                        if (!flag) {
                            UserByPermissionVO vo = new UserByPermissionVO();
                            vo.setUserId(user.getId());
                            vo.setUserName(user.getNickname());
                            vo.setRoleStr(role.getName());
                            vo.setCode(user.getUsername());
                            vo.setMobile(user.getMobile());
                            ansList.add(vo);
                        }
                    }
                }
            }
        }
        return new ResultUtil<List<UserByPermissionVO>>().setData(ansList);
    }

    private List<Permission> getPermissionByUserId(String userId) {
        QueryWrapper<UserRole> urQw = new QueryWrapper<>();
        urQw.eq("userId", userId);
        List<UserRole> userRoleList = iUserRoleService.list(urQw);
        List<Permission> permissionList = new ArrayList<>();
        for (UserRole userRole : userRoleList) {
            QueryWrapper<RolePermission> rpQw = new QueryWrapper<>();
            rpQw.eq("roleId", userRole.getRoleId());
            List<RolePermission> rolePermissionList = iRolePermissionService.list(rpQw);
            for (RolePermission rolePermission : rolePermissionList) {
                boolean flag = true;
                for (Permission permission : permissionList) {
                    if (Objects.equals(permission.getId(), rolePermission.getPermissionId())) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    permissionList.add(iPermissionService.getById(rolePermission.getPermissionId()));
                }
            }
        }
        return permissionList;
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询有权限的菜单", logType = LogType.DATA_CENTER, doType = "PERMISSION-02")
    @Operation(summary = "查询有权限的菜单", description = "查询有权限的菜单")
    @RequestMapping(value = "/getMenuList", method = RequestMethod.GET)
    public Result<List<MenuVO>> getMenuList() {
        List<MenuVO> menuList = new ArrayList<>();
        User currUser = securityUtil.getCurrUser();
        String keyInRedis = "permission::userMenuList:" + currUser.getId();
        String valueInRedis = stringRedisTemplate.opsForValue().get(keyInRedis);
        if (!NullUtils.isNull(valueInRedis)) {
            menuList = new Gson().fromJson(valueInRedis, new TypeToken<List<MenuVO>>() {
            }.getType());
            return new ResultUtil<List<MenuVO>>().setData(menuList);
        }
        // 拥有的菜单列表
        List<Permission> list = getPermissionByUserId(currUser.getId());
        // 顶级菜单
        for (Permission permission : list) {
            if (CommonConstant.PERMISSION_NAV.equals(permission.getType()) && CommonConstant.LEVEL_ZERO.equals(permission.getLevel())) {
                menuList.add(VoUtil.permissionToMenuVo(permission));
            }
        }
        // 一级菜单
        List<MenuVO> firstMenuList = new ArrayList<>();
        for (Permission permission : list) {
            if (Objects.equals(CommonConstant.PERMISSION_PAGE, permission.getType()) && Objects.equals(CommonConstant.LEVEL_ONE, permission.getLevel())) {
                firstMenuList.add(VoUtil.permissionToMenuVo(permission));
            }
        }
        // 二级菜单
        List<MenuVO> secondMenuList = new ArrayList<>();
        for (Permission permission : list) {
            if (Objects.equals(CommonConstant.PERMISSION_PAGE, permission.getType()) && Objects.equals(CommonConstant.LEVEL_TWO, permission.getLevel())) {
                secondMenuList.add(VoUtil.permissionToMenuVo(permission));
            }
        }
        // 按钮
        List<MenuVO> buttonPermissions = new ArrayList<>();
        for (Permission permission : list) {
            if (Objects.equals(CommonConstant.PERMISSION_OPERATION, permission.getType()) && Objects.equals(CommonConstant.LEVEL_THREE, permission.getLevel())) {
                buttonPermissions.add(VoUtil.permissionToMenuVo(permission));
            }
        }
        // 有权限的二级菜单
        for (MenuVO vo : secondMenuList) {
            List<String> permTypes = new ArrayList<>();
            for (MenuVO menuVo : buttonPermissions) {
                if (Objects.equals(vo.getId(), menuVo.getParentId())) {
                    permTypes.add(menuVo.getButtonType());
                }
            }
            vo.setPermTypes(permTypes);
        }
        // 二连一
        for (MenuVO vo : firstMenuList) {
            List<MenuVO> secondMenu = new ArrayList<>();
            for (MenuVO menuVo : secondMenuList) {
                if (Objects.equals(vo.getId(), menuVo.getParentId())) {
                    secondMenu.add(menuVo);
                }
            }
            vo.setChildren(secondMenu);
        }
        // 一连顶
        for (MenuVO vo : menuList) {
            List<MenuVO> firstMenu = new ArrayList<>();
            for (MenuVO menuVo : firstMenuList) {
                if (Objects.equals(vo.getId(), menuVo.getParentId())) {
                    firstMenu.add(menuVo);
                }
            }
            vo.setChildren(firstMenu);
        }
        stringRedisTemplate.opsForValue().set(keyInRedis, new Gson().toJson(menuList), 10L, TimeUnit.DAYS);
        return new ResultUtil<List<MenuVO>>().setData(menuList);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "搜索菜单", logType = LogType.DATA_CENTER, doType = "PERMISSION-03")
    @Operation(summary = "搜索菜单", description = "搜索菜单")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Result<List<Permission>> searchPermissionList(@RequestParam String title) {
        QueryWrapper<Permission> qw = new QueryWrapper<>();
        qw.like("title", title);
        qw.orderByAsc("sortOrder");
        return new ResultUtil<List<Permission>>().setData(iPermissionService.list(qw));
    }

    @Operation(summary = "根据父ID查询菜单")
    private List<Permission> getPermissionListByParentId(String parentId) {
        QueryWrapper<Permission> qw = new QueryWrapper<>();
        qw.eq("parentId", parentId);
        qw.orderByAsc("sortOrder");
        return iPermissionService.list(qw);
    }

    @Operation(summary = "根据层级查询菜单")
    private List<Permission> getPermissionListByLevel(int level) {
        QueryWrapper<Permission> qw = new QueryWrapper<>();
        qw.eq("level", level);
        qw.orderByAsc("sortOrder");
        return iPermissionService.list(qw);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询全部菜单", logType = LogType.DATA_CENTER, doType = "PERMISSION-04")
    @Operation(summary = "查询全部菜单", description = "查询全部菜单（获取权限菜单树）")
    @Cacheable(key = "'allList'")
    @RequestMapping(value = "/getAllList", method = RequestMethod.GET)
    public Result<List<Permission>> getAllList() {
        // 顶级菜单列表
        List<Permission> list0 = getPermissionListByLevel(0);
        for (Permission p0 : list0) {
            // 一级
            List<Permission> list1 = getPermissionListByParentId(p0.getId());
            p0.setChildren(list1);
            // 二级
            for (Permission p1 : list1) {
                List<Permission> children1 = getPermissionListByParentId(p1.getId());
                p1.setChildren(children1);
                // 三级
                for (Permission p2 : children1) {
                    List<Permission> children2 = getPermissionListByParentId(p2.getId());
                    p2.setChildren(children2);
                }
            }
        }
        return new ResultUtil<List<Permission>>().setData(list0);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "删除菜单", logType = LogType.DATA_CENTER, doType = "PERMISSION-05")
    @Operation(summary = "删除菜单", description = "删除菜单")
    @CacheEvict(key = "'menuList'")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result<Object> delByIds(@RequestParam String[] ids) {
        for (String id : ids) {
            QueryWrapper<RolePermission> qw = new QueryWrapper<>();
            qw.like("permissionId", id);
            long rolePermissionCount = iRolePermissionService.count(qw);
            if (rolePermissionCount > 0L) {
                Permission permission = iPermissionService.getById(id);
                if (permission == null) {
                    return ResultUtil.error("该空菜单正在被角色使用，不能删除");
                }
                return ResultUtil.error(permission.getTitle() + "菜单正在被角色使用，不能删除");
            }
        }
        for (String id : ids) {
            iPermissionService.removeById(id);
        }
        mySecurityMetadataSource.loadResourceDefine();
        stringRedisTemplate.delete("permission::allList");
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "添加菜单", logType = LogType.DATA_CENTER, doType = "PERMISSION-06")
    @Operation(summary = "添加菜单", description = "添加菜单")
    @CacheEvict(key = "'menuList'")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<Permission> add(Permission permission) {
        if (Objects.equals(CommonConstant.PERMISSION_OPERATION, permission.getType())) {
            QueryWrapper<Permission> perQw = new QueryWrapper<>();
            perQw.eq("title", permission.getTitle());
            long permissionByTitleCount = iPermissionService.count();
            if (permissionByTitleCount > 0L) {
                return new ResultUtil<Permission>().setErrorMsg("名称已存在");
            }
        }
        iPermissionService.saveOrUpdate(permission);
        mySecurityMetadataSource.loadResourceDefine();
        stringRedisTemplate.delete("permission::allList");
        return new ResultUtil<Permission>().setData(permission);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "编辑菜单", logType = LogType.DATA_CENTER, doType = "PERMISSION-07")
    @Operation(summary = "编辑菜单", description = "编辑菜单")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public Result<Permission> edit(Permission permission) {
        if (Objects.equals(CommonConstant.PERMISSION_OPERATION, permission.getType())) {
            Permission p = iPermissionService.getById(permission.getId());
            if (!Objects.equals(p.getTitle(), permission.getTitle())) {
                QueryWrapper<Permission> perQw = new QueryWrapper<>();
                perQw.eq("title", permission.getTitle());
                long permissionCount = iPermissionService.count(perQw);
                if (permissionCount > 0L) {
                    return new ResultUtil<Permission>().setErrorMsg("名称已存在");
                }
            }
        }
        iPermissionService.saveOrUpdate(permission);
        mySecurityMetadataSource.loadResourceDefine();
        Set<String> keysUser = redisTemplateHelper.keys("user:" + "*");
        stringRedisTemplate.delete(keysUser);
        Set<String> keysUserMenu = redisTemplateHelper.keys("permission::userMenuList:*");
        stringRedisTemplate.delete(keysUserMenu);
        stringRedisTemplate.delete("permission::allList");
        return new ResultUtil<Permission>().setData(permission);
    }

}
