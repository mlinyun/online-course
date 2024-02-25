package com.mlinyun.onlinecourse.data.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.constant.CommonConstant;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.redis.RedisTemplateHelper;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.PageUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.basics.utils.SecurityUtil;
import com.mlinyun.onlinecourse.data.dto.PermissionDTO;
import com.mlinyun.onlinecourse.data.dto.RoleDTO;
import com.mlinyun.onlinecourse.data.entity.*;
import com.mlinyun.onlinecourse.data.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "用户接口")
@RequestMapping("/user")
@CacheConfig(cacheNames = "user")
@RestController
@Transactional
public class UserController {

    @Resource
    private IUserService iUserService;

    @Resource
    private IDepartmentService iDepartmentService;

    @Resource
    private IRoleService iRoleService;

    @Resource
    private IUserRoleService iUserRoleService;

    @Resource
    private IDepartmentHeaderService iDepartmentHeaderService;

    @Resource
    private IRolePermissionService iRolePermissionService;

    @Resource
    private RedisTemplateHelper redisTemplateHelper;

    @Resource
    private IPermissionService iPermissionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SecurityUtil securityUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_PRE_1 = "userRole::";

    private static final String REDIS_PRE_2 = "userRole::depIds:";

    private static final String REDIS_PRE_3 = "permission::userMenuList:";

    private static final String REDIS_PRE_4 = "user::";

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "获取当前登录用户", logType = LogType.DATA_CENTER, doType = "USER-02")
    @Operation(summary = "获取当前登录用户", description = "获取当前登录用户")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Result<User> getUserInfo() {
        User u = securityUtil.getCurrUser();
        entityManager.clear();
        u.setPassword(null);
        return new ResultUtil<User>().setData(u);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "注册用户", logType = LogType.DATA_CENTER, doType = "USER-03")
    @Operation(summary = "注册用户", description = "注册用户")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Result<Object> register(@Valid User u) {
        u.setEmail(u.getMobile() + "@qq.com");
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.and(wrapper -> wrapper.eq("username", u.getUsername()).or().eq("mobile", u.getMobile()));
        if (iUserService.count(userQw) > 0L) {
            return ResultUtil.error("登录账号/手机号重复");
        }
        String encryptPass = new BCryptPasswordEncoder().encode(u.getPassword());
        u.setPassword(encryptPass).setType(0);
        iUserService.saveOrUpdate(u);
        QueryWrapper<Role> roleQw = new QueryWrapper<>();
        roleQw.eq("defaultRole", true);
        List<Role> roleList = iRoleService.list(roleQw);
        if (!roleList.isEmpty()) {
            for (Role role : roleList) {
                iUserRoleService.saveOrUpdate(new UserRole().setUserId(u.getId()).setRoleId(role.getId()));
            }
        }
        return ResultUtil.data(u);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "解锁验证密码", logType = LogType.DATA_CENTER, doType = "USER-04")
    @Operation(summary = "解锁验证密码", description = "解锁验证密码")
    @RequestMapping(value = "/unlock", method = RequestMethod.POST)
    public Result<Object> unLock(@RequestParam String password) {
        User u = securityUtil.getCurrUser();
        if (!new BCryptPasswordEncoder().matches(password, u.getPassword())) {
            return ResultUtil.error("密码不正确");
        }
        return ResultUtil.data(null);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "重置密码", logType = LogType.DATA_CENTER, doType = "USER-05")
    @Operation(summary = "重置密码", description = "重置密码")
    @RequestMapping(value = "/resetPass", method = RequestMethod.POST)
    public Result<Object> resetPass(@RequestParam String[] ids) {
        for (String id : ids) {
            User userForReset = iUserService.getById(id);
            if (userForReset == null) {
                return ResultUtil.error("不存在");
            }
            userForReset.setPassword(new BCryptPasswordEncoder().encode("123456"));
            iUserService.saveOrUpdate(userForReset);
            stringRedisTemplate.delete(REDIS_PRE_4 + userForReset.getUsername());
        }
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "修改用户资料", logType = LogType.DATA_CENTER, doType = "USER-06")
    @Operation(summary = "修改用户资料", description = "用户名密码不会修改 需要username更新缓存")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @CacheEvict(key = "#u.username")
    public Result<Object> editOwn(User u) {
        User old = securityUtil.getCurrUser();
        u.setUsername(old.getUsername());
        u.setPassword(old.getPassword());
        iUserService.saveOrUpdate(u);
        return ResultUtil.success("修改成功");
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "修改密码", logType = LogType.DATA_CENTER, doType = "USER-07")
    @Operation(summary = "修改密码", description = "修改密码")
    @RequestMapping(value = "/modifyPass", method = RequestMethod.POST)
    public Result<Object> modifyPass(@RequestParam String password, @RequestParam String newPass, @RequestParam String passStrength) {
        User user = securityUtil.getCurrUser();
        if (!new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            return ResultUtil.error("原密码不正确");
        }
        String newEncryptPass = new BCryptPasswordEncoder().encode(newPass);
        user.setPassword(newEncryptPass);
        user.setPassStrength(passStrength);
        iUserService.saveOrUpdate(user);
        stringRedisTemplate.delete(REDIS_PRE_4 + user.getUsername());
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询用户", logType = LogType.DATA_CENTER, doType = "USER-08")
    @Operation(summary = "查询用户", description = "查询用户")
    @RequestMapping(value = "/getUserList", method = RequestMethod.GET)
    public Result<IPage<User>> getUserList(@ModelAttribute User user, @ModelAttribute PageVo page) {
        QueryWrapper<User> userQw = new QueryWrapper<>();
        if (!NullUtils.isNull(user.getNickname())) {
            userQw.like("nickname", user.getNickname());
        }
        if (!NullUtils.isNull(user.getDepartmentId())) {
            userQw.eq("departmentId", user.getDepartmentId());
        }
        IPage<User> userData = iUserService.page(PageUtil.initMpPage(page), userQw);
        for (User u : userData.getRecords()) {
            QueryWrapper<Role> roleQw = new QueryWrapper<>();
            roleQw.inSql("id", "SELECT roleId FROM user_role WHERE userId = '" + u.getId() + "'");
            List<Role> list = iRoleService.list(roleQw);
            List<RoleDTO> roleDTOList = list.stream().map(e -> {
                return new RoleDTO().setId(e.getId()).setName(e.getName()).setDescription(e.getDescription());
            }).collect(Collectors.toList());
            u.setRoles(roleDTOList);
            entityManager.detach(u);
            u.setPassword(null);
        }
        return new ResultUtil<IPage<User>>().setData(userData);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "根据部门查询用户", logType = LogType.DATA_CENTER, doType = "USER-09")
    @Operation(summary = "根据部门查询用户", description = "根据部门查询用户")
    @RequestMapping(value = "/getByDepartmentId", method = RequestMethod.GET)
    public Result<List<User>> getByCondition(@RequestParam String departmentId) {
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("departmentId", departmentId);
        List<User> list = iUserService.list(userQw);
        entityManager.clear();
        list.forEach(u -> {
            u.setPassword(null);
        });
        return new ResultUtil<List<User>>().setData(list);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "模拟搜索用户", logType = LogType.DATA_CENTER, doType = "USER-10")
    @Operation(summary = "模拟搜索用户", description = "模拟搜索用户")
    @RequestMapping(value = "/searchByName/{username}", method = RequestMethod.GET)
    public Result<List<User>> searchByName(@PathVariable String username) throws UnsupportedEncodingException {
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.eq("username", URLDecoder.decode(username, "utf-8"));
        userQw.eq("status", 0);
        List<User> list = iUserService.list(userQw);
        entityManager.clear();
        list.forEach(u -> {
            u.setPassword(null);
        });
        return new ResultUtil<List<User>>().setData(list);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询全部用户", logType = LogType.DATA_CENTER, doType = "USER-11")
    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    @Operation(summary = "查询全部用户")
    public Result<List<User>> getAll() {
        List<User> userList = iUserService.list();
        for (User user : userList) {
            entityManager.clear();
            user.setPassword(null);
        }
        return new ResultUtil<List<User>>().setData(userList);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "管理员修改资料", logType = LogType.DATA_CENTER, doType = "USER-12")
    @Operation(summary = "管理员修改资料", description = "管理员修改资料")
    @RequestMapping(value = "/admin/edit", method = RequestMethod.POST)
    @CacheEvict(key = "#u.username")
    public Result<Object> edit(User u, @RequestParam(required = false) String[] roleIds) {
        User customaryUser = iUserService.getById(u.getId());
        // 登录账号和密码不能发生变更
        u.setUsername(customaryUser.getUsername());
        u.setPassword(customaryUser.getPassword());
        if (!Objects.equals(customaryUser.getMobile(), u.getMobile())) {
            QueryWrapper<User> customaryUserQw = new QueryWrapper<>();
            customaryUserQw.ne("id", customaryUser.getId());
            customaryUserQw.eq("mobile", u.getMobile());
            long customaryUserCount = iUserService.count(customaryUserQw);
            if (customaryUserCount > 0) {
                return ResultUtil.error("手机号重复");
            }
        }
        if (!NullUtils.isNull(u.getDepartmentId())) {
            Department department = iDepartmentService.getById(u.getDepartmentId());
            if (department != null) {
                u.setDepartmentTitle(department.getTitle());
            }
        } else {
            u.setDepartmentId("");
            u.setDepartmentTitle("");
        }
        iUserService.saveOrUpdate(u);
        QueryWrapper<UserRole> userRoleQw = new QueryWrapper<>();
        userRoleQw.eq("userId", u.getId());
        iUserRoleService.remove(userRoleQw);
        if (roleIds != null && roleIds.length > 0) {
            for (String roleId : roleIds) {
                UserRole ur = new UserRole();
                ur.setUserId(u.getId());
                ur.setRoleId(roleId);
                iUserRoleService.saveOrUpdate(ur);
            }
        }
        stringRedisTemplate.delete(REDIS_PRE_1 + u.getId());
        stringRedisTemplate.delete(REDIS_PRE_2 + u.getId());
        stringRedisTemplate.delete(REDIS_PRE_3 + u.getId());
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "添加用户", logType = LogType.DATA_CENTER, doType = "USER-13")
    @Operation(summary = "添加用户", description = "添加用户")
    @RequestMapping(value = "/admin/add", method = RequestMethod.POST)
    public Result<Object> add(@Valid User u, @RequestParam(required = false) String[] roleIds) {
        QueryWrapper<User> userQw = new QueryWrapper<>();
        userQw.and(wrapper -> wrapper.eq("username", u.getUsername()).or().eq("mobile", u.getMobile()));
        if (iUserService.count(userQw) > 0L) {
            return ResultUtil.error("登录账号/手机号重复");
        }
        if (!NullUtils.isNull(u.getDepartmentId())) {
            Department department = iDepartmentService.getById(u.getDepartmentId());
            if (department != null) {
                u.setDepartmentTitle(department.getTitle());
            }
        } else {
            u.setDepartmentId("");
            u.setDepartmentTitle("");
        }
        u.setPassword(new BCryptPasswordEncoder().encode(u.getPassword()));
        iUserService.saveOrUpdate(u);
        if (roleIds != null && roleIds.length > 0) {
            for (String roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(u.getId());
                userRole.setRoleId(roleId);
                iUserRoleService.saveOrUpdate(userRole);
            }
        }
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "禁用用户", logType = LogType.DATA_CENTER, doType = "USER-14")
    @Operation(summary = "禁用用户", description = "禁用用户")
    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    public Result<Object> disable(@RequestParam String id) {
        User user = iUserService.getById(id);
        if (user == null) {
            return ResultUtil.error("用户不存在");
        }
        user.setStatus(CommonConstant.USER_STATUS_LOCK);
        iUserService.saveOrUpdate(user);
        stringRedisTemplate.delete("user::" + user.getUsername());
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "启用用户", logType = LogType.DATA_CENTER, doType = "USER-15")
    @Operation(summary = "启用用户", description = "启用用户")
    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    public Result<Object> enable(@RequestParam String id) {
        User user = iUserService.getById(id);
        if (user == null) {
            return ResultUtil.error("用户不存在");
        }
        user.setStatus(CommonConstant.USER_STATUS_NORMAL);
        iUserService.saveOrUpdate(user);
        stringRedisTemplate.delete("user::" + user.getUsername());
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "删除用户", logType = LogType.DATA_CENTER, doType = "USER-16")
    @Operation(summary = "删除用户", description = "删除用户")
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result<Object> delByIds(@RequestParam String[] ids) {
        for (String id : ids) {
            User u = iUserService.getById(id);
            stringRedisTemplate.delete("user::" + u.getUsername());
            stringRedisTemplate.delete("userRole::" + u.getId());
            stringRedisTemplate.delete("userRole::depIds:" + u.getId());
            stringRedisTemplate.delete("permission::userMenuList:" + u.getId());
            Set<String> keys = redisTemplateHelper.keys("department::*");
            stringRedisTemplate.delete(keys);
            iUserService.removeById(id);
            QueryWrapper<UserRole> urQw = new QueryWrapper<>();
            urQw.eq("userId", id);
            iUserRoleService.remove(urQw);
            QueryWrapper<DepartmentHeader> dhQw = new QueryWrapper<>();
            dhQw.eq("userId", id);
            iDepartmentHeaderService.remove(dhQw);
        }
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "导入用户", logType = LogType.DATA_CENTER, doType = "USER-17")
    @Operation(summary = "导入用户", description = "导入用户")
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    public Result<Object> importData(@RequestBody List<User> users) {
        List<Integer> errors = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        int count = 0;
        for (User u : users) {
            count++;
            if (StrUtil.isBlank(u.getUsername()) || StrUtil.isBlank(u.getPassword())) {
                errors.add(count);
                reasons.add("账号密码为空");
                continue;
            }

            QueryWrapper<User> userQw = new QueryWrapper<>();
            userQw.eq("username", u.getUsername());
            if (iUserService.count(userQw) > 0L) {
                errors.add(count);
                reasons.add("用户名已存在");
                continue;
            }
            u.setPassword(new BCryptPasswordEncoder().encode(u.getPassword()));
            if (StrUtil.isNotBlank(u.getDepartmentId())) {
                Department department = iDepartmentService.getById(u.getDepartmentId());
                if (department == null) {
                    errors.add(count);
                    reasons.add("部门不存在");
                    continue;
                }
            }
            if (u.getStatus() == null) {
                u.setStatus(CommonConstant.USER_STATUS_NORMAL);
            }
            iUserService.saveOrUpdate(u);
            if (u.getDefaultRole() != null && u.getDefaultRole() == 1) {
                QueryWrapper<Role> roleQw = new QueryWrapper<>();
                roleQw.eq("defaultRole", true);
                List<Role> roleList = iRoleService.list(roleQw);
                if (roleList != null && roleList.size() > 0) {
                    for (Role role : roleList) {
                        UserRole ur = new UserRole().setUserId(u.getId()).setRoleId(role.getId());
                        iUserRoleService.saveOrUpdate(ur);
                    }
                }
            }
        }
        int successCount = users.size() - errors.size();
        String successMessage = "成功导入 " + successCount + " 位用户";
        String failMessage = "成功导入 " + successCount + " 位用户，失败 " + errors.size() + " 位用户。<br>" + "第 " + errors.toString() + " 行数据导入出错，错误原因是为 <br>" + reasons.toString();
        String message = null;
        if (errors.size() == 0) {
            message = successMessage;
        } else {
            message = failMessage;
        }
        return ResultUtil.success(message);
    }

    @Operation(summary = "添加用户的角色和菜单信息")
    public User userToDTO(User user) {
        if (user == null) {
            return null;
        }
        // 角色
        QueryWrapper<UserRole> urQw = new QueryWrapper<>();
        urQw.eq("userId", user.getId());
        List<UserRole> roleList = iUserRoleService.list(urQw);
        List<RoleDTO> roleDTOList = new ArrayList<>();
        for (UserRole userRole : roleList) {
            Role role = iRoleService.getById(userRole.getRoleId());
            if (role != null) {
                roleDTOList.add(new RoleDTO().setId(role.getId()).setName(role.getName()));
            }
        }
        user.setRoles(roleDTOList);
        // 菜单
        List<String> permissionIdList = new ArrayList<>();
        for (RoleDTO dto : roleDTOList) {
            QueryWrapper<RolePermission> rpQw = new QueryWrapper<>();
            rpQw.eq("roleId", dto.getId());
            List<RolePermission> list = iRolePermissionService.list(rpQw);
            for (RolePermission rp : list) {
                boolean flag = true;
                for (String id : permissionIdList) {
                    if (Objects.equals(id, rp.getPermissionId())) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    permissionIdList.add(rp.getPermissionId());
                }
            }
        }
        List<PermissionDTO> permissionDTOList = new ArrayList<>();
        for (String id : permissionIdList) {
            Permission permission = iPermissionService.getById(id);
            if (permission != null) {
                if (Objects.equals(permission.getType(), CommonConstant.PERMISSION_OPERATION)) {
                    continue;
                }
                permissionDTOList.add(new PermissionDTO().setTitle(permission.getTitle()).setPath(permission.getPath()));
            }
        }
        user.setPermissions(permissionDTOList);
        return user;
    }

}
