package com.mlinyun.onlinecourse.data.controller;

import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.google.gson.Gson;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.CommonUtil;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.entity.File;
import com.mlinyun.onlinecourse.data.service.IFileService;
import com.mlinyun.onlinecourse.data.service.ISettingService;
import com.mlinyun.onlinecourse.data.utils.file.Base64DecodeMultipartFile;
import com.mlinyun.onlinecourse.data.utils.file.LocalFileManage;
import com.mlinyun.onlinecourse.data.vo.OssSettingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


@Tag(name = "文件上传接口")
@RequestMapping("/upload")
@CacheConfig(cacheNames = "upload")
@RestController
@Transactional
public class UploadController {

    @Resource
    private LocalFileManage localFileManage;

    @Resource
    private ISettingService iSettingService;

    @Resource
    private IFileService iFileService;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "文件上传", logType = LogType.DATA_CENTER, doType = "Upload-01")
    @Operation(summary = "文件上传", description = "文件上传")
    @RequestMapping(value = "/file", method = RequestMethod.POST)
    public Result<Object> upload(@RequestParam(required = false) MultipartFile file, @RequestParam(required = false) String base64) {
        if (StrUtil.isNotBlank(base64)) {
            file = Base64DecodeMultipartFile.base64Convert(base64);
        }
        String result = null;
        String fKey = CommonUtil.renamePic(file.getOriginalFilename());
        File f = new File();
        try {
            InputStream inputStream = file.getInputStream();
            result = localFileManage.inputStreamUpload(inputStream, fKey, file);
            f.setLocation(0);
            f.setName(file.getOriginalFilename());
            f.setSize(file.getSize());
            f.setType(file.getContentType());
            f.setFKey(fKey);
            f.setUrl(result);
            iFileService.saveOrUpdate(f);
        } catch (Exception e) {
            return ResultUtil.error(e.toString());
        }
        OssSettingVO ossSettingVO = new Gson().fromJson(iSettingService.getById("LOCAL_OSS").getValue(), OssSettingVO.class);
        return ResultUtil.data(ossSettingVO.getHttp() + ossSettingVO.getEndpoint() + "/" + f.getId());
    }

}
