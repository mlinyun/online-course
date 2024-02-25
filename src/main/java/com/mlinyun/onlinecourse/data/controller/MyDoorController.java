package com.mlinyun.onlinecourse.data.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.basics.utils.SecurityUtil;
import com.mlinyun.onlinecourse.data.entity.Permission;
import com.mlinyun.onlinecourse.data.entity.User;
import com.mlinyun.onlinecourse.data.service.IPermissionService;
import com.mlinyun.onlinecourse.data.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Tag(name = "个人门户接口")
@RequestMapping("/myDoor")
@CacheConfig(cacheNames = "myDoor")
@RestController
@Transactional
public class MyDoorController {

    @Resource
    private SecurityUtil securityUtil;

    @Resource
    private IPermissionService iPermissionService;

    @Resource
    private IUserService iUserService;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询个人门户菜单A", logType = LogType.DATA_CENTER, doType = "MY-DOOR-01")
    @Operation(summary = "查询个人门户菜单A", description = "查询个人门户菜单A")
    @RequestMapping(value = "/getMyDoorList", method = RequestMethod.POST)
    public Result<List<MyDoorMenuClass>> getMyDoorList() {
        User user = securityUtil.getCurrUser();
        user = iUserService.getById(user.getId());
        List<MyDoorMenuClass> ans = new ArrayList<>();
        String myDoor = user.getMyDoor();
        if (NullUtils.isNull(myDoor)) {
            return new ResultUtil().setData(ans);
        }
        String[] zwz666s = myDoor.split("ZWZ666");
        List<Permission> all = iPermissionService.list();
        for (String zwz666 : zwz666s) {
            for (Permission permission : all) {
                if (Objects.equals(permission.getName(), zwz666)) {
                    MyDoorMenuClass menu = new MyDoorMenuClass();
                    menu.setName(permission.getName());
                    menu.setTitle(permission.getTitle());
                    ans.add(menu);
                    break;
                }
            }
        }
        return new ResultUtil().setData(ans);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询个人门户菜单B", logType = LogType.DATA_CENTER, doType = "MY-DOOR-02")
    @Operation(summary = "获取个人门户菜单B", description = "获取个人门户菜单B")
    @RequestMapping(value = "/getMyDoorList6", method = RequestMethod.POST)
    public Result<List<MyDoorMenuClass>> getMyDoorList6() {
        User user = securityUtil.getCurrUser();
        user = iUserService.getById(user.getId());
        List<MyDoorMenuClass> ans = new ArrayList<>();
        String myDoor = user.getMyDoor();
        if (NullUtils.isNull(myDoor)) {
            ans.add(getNullMenu());
            ans.add(getNullMenu());
            ans.add(getNullMenu());
            ans.add(getNullMenu());
            ans.add(getNullMenu());
            ans.add(getNullMenu());
            return new ResultUtil().setData(ans);
        }
        String[] zwz666s = myDoor.split("ZWZ666");
        List<Permission> all = iPermissionService.list();
        for (String zwz666 : zwz666s) {
            for (Permission permission : all) {
                if (Objects.equals(permission.getName(), zwz666)) {
                    MyDoorMenuClass menu = new MyDoorMenuClass();
                    menu.setName(permission.getName());
                    menu.setTitle(permission.getTitle());
                    ans.add(menu);
                    break;
                }
            }
        }
        int size = ans.size();
        if (size < 6) {
            int time = 6 - size;
            for (int i = 0; i < time; i++) {
                ans.add(getNullMenu());
            }
        }
        return new ResultUtil().setData(ans);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "修改个人门户菜单", logType = LogType.DATA_CENTER, doType = "MY-DOOR-03")
    @Operation(summary = "修改个人门户菜单", description = "修改个人门户菜单")
    @RequestMapping(value = "/setMyDoorList", method = RequestMethod.POST)
    public Result<Object> setMyDoorList(@RequestParam String str) {
        User user = securityUtil.getCurrUser();
        user = iUserService.getById(user.getId());
        if (user != null) {
            if (NullUtils.isNull(str)) {
                user.setMyDoor("");
                iUserService.saveOrUpdate(user);
            } else {
                user.setMyDoor(str);
                iUserService.saveOrUpdate(user);
            }
            return ResultUtil.success("OK");
        }
        return ResultUtil.error("ROSTER IS NULL");
    }

    private MyDoorMenuClass getNullMenu() {
        MyDoorMenuClass menu = new MyDoorMenuClass();
        menu.setName("null");
        menu.setTitle("尚未添加");
        return menu;
    }

    @Data
    private class MyDoorMenuClass {
        private String name;
        private String title;
    }

}
