package com.mlinyun.onlinecourse.data.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.google.gson.Gson;
import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.constant.SettingConstant;
import com.mlinyun.onlinecourse.basics.exception.RuntimeExceptionHandler;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.NullUtils;
import com.mlinyun.onlinecourse.basics.utils.PageUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.entity.File;
import com.mlinyun.onlinecourse.data.entity.User;
import com.mlinyun.onlinecourse.data.service.IFileService;
import com.mlinyun.onlinecourse.data.service.ISettingService;
import com.mlinyun.onlinecourse.data.service.IUserService;
import com.mlinyun.onlinecourse.data.utils.file.LocalFileManage;
import com.mlinyun.onlinecourse.data.vo.OssSettingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Tag(name = "文件管理接口")
@RequestMapping("/file")
@CacheConfig(cacheNames = "file")
@Controller
@Transactional
public class FileController {

    @Resource
    private LocalFileManage localFileManage;

    @Resource
    private IUserService iUserService;

    @Resource
    private IFileService iFileService;

    @Resource
    private ISettingService iSettingService;

    @PersistenceContext
    private EntityManager entityManager;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查询系统文件", logType = LogType.DATA_CENTER, doType = "FILE-01")
    @Operation(summary = "查询系统文件", description = "查询系统文件")
    @ResponseBody
    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    public Result<IPage<File>> getByCondition(@ModelAttribute File file, @ModelAttribute PageVo page) {
        QueryWrapper<File> qw = new QueryWrapper<>();
        if (!NullUtils.isNull(file.getFKey())) {
            qw.eq("fKey", file.getFKey());
        }
        if (!NullUtils.isNull(file.getType())) {
            qw.eq("type", file.getType());
        }
        if (!NullUtils.isNull(file.getName())) {
            qw.eq("name", file.getName());
        }
        IPage<File> fileList = iFileService.page(PageUtil.initMpPage(page), qw);
        OssSettingVO os = new Gson().fromJson(iSettingService.getById(SettingConstant.LOCAL_OSS).getValue(), OssSettingVO.class);
        Map<String, String> map = new HashMap<>(16);
        for (File e : fileList.getRecords()) {
            if (e.getLocation() != null && Objects.equals(0, e.getLocation())) {
                String url = os.getHttp() + os.getEndpoint() + "/";
                entityManager.detach(e);
                e.setUrl(url + e.getId());
            }
            if (StrUtil.isNotBlank(e.getCreateBy())) {
                if (!map.containsKey(e.getCreateBy())) {
                    QueryWrapper<User> userQw = new QueryWrapper<>();
                    userQw.eq("username", e.getCreateBy());
                    User u = iUserService.getOne(userQw);
                    if (u != null) {
                        e.setNickname(u.getNickname());
                    }
                    map.put(e.getCreateBy(), u.getNickname());
                } else {
                    e.setNickname(map.get(e.getCreateBy()));
                }
            }
        }
        map = null;
        return new ResultUtil<IPage<File>>().setData(fileList);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "文件复制", logType = LogType.DATA_CENTER, doType = "FILE-02")
    @Operation(summary = "文件复制", description = "文件复制")
    @ResponseBody
    @RequestMapping(value = "/copy", method = RequestMethod.POST)
    public Result<Object> copy(@RequestParam String id, @RequestParam String key) {
        File file = iFileService.getById(id);
        if (file.getLocation() == null) {
            file.setLocation(0);
        }
        String toKey = "副本_" + key;
        key = file.getUrl();
        String newUrl = localFileManage.copyFile(key, toKey);
        File newFile = new File().setName(file.getName()).setFKey(toKey).setSize(file.getSize()).setType(file.getType()).setLocation(file.getLocation()).setUrl(newUrl);
        iFileService.saveOrUpdate(newFile);
        return ResultUtil.data();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "文件重命名", logType = LogType.DATA_CENTER, doType = "FILE-03")
    @Operation(summary = "文件重命名", description = "文件重命名")
    @ResponseBody
    @RequestMapping(value = "/rename", method = RequestMethod.POST)
    public Result<Object> upload(@RequestParam String id, @RequestParam String newKey, @RequestParam String newName) {
        File file = iFileService.getById(id);
        if (file.getLocation() == null) {
            file.setLocation(0);
        }
        String newUrl = "";
        String oldKey = file.getFKey();
        if (!Objects.equals(newKey, oldKey)) {
            oldKey = file.getUrl();
            newUrl = localFileManage.renameFile(oldKey, newKey);
        }
        file.setName(newName);
        file.setFKey(newKey);
        if (!oldKey.equals(newKey)) {
            file.setUrl(newUrl);
        }
        iFileService.saveOrUpdate(file);
        return ResultUtil.data();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "文件删除", logType = LogType.DATA_CENTER, doType = "FILE-04")
    @Operation(summary = "文件删除", description = "文件删除")
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result<Object> delete(@RequestParam String[] ids) {
        for (String id : ids) {
            File file = iFileService.getById(id);
            if (file.getLocation() == null) {
                file.setLocation(0);
            }
            String key = file.getUrl();
            localFileManage.deleteFile(key);
            iFileService.removeById(id);
        }
        return ResultUtil.data();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "本地存储预览文件", logType = LogType.DATA_CENTER, doType = "FILE-05")
    @Operation(summary = "本地存储预览文件", description = "本地存储预览文件")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public void view(@PathVariable String id, @RequestParam(required = false) String filename, @RequestParam(required = false, defaultValue = "false") Boolean preview, HttpServletResponse httpServletResponse) throws IOException {
        File selectFile = iFileService.getById(id);
        if (selectFile == null) {
            throw new RuntimeExceptionHandler("文件不存在");
        }
        if (NullUtils.isNull(filename)) {
            filename = selectFile.getFKey();
        }
        if (!preview) {
            httpServletResponse.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        }
        httpServletResponse.setContentLengthLong(selectFile.getSize());
        httpServletResponse.setContentType(selectFile.getType());
        httpServletResponse.addHeader("Accept-Ranges", "bytes");
        if (selectFile.getSize() != null && selectFile.getSize() > 0) {
            httpServletResponse.addHeader("Content-Range", "bytes " + 0 + "-" + (selectFile.getSize() - 1) + "/" + selectFile.getSize());
        }
        LocalFileManage.view(selectFile.getUrl(), httpServletResponse);
    }

}
