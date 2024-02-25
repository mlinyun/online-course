package com.mlinyun.onlinecourse.data.controller;

import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.google.gson.Gson;
import com.mlinyun.onlinecourse.basics.baseVo.Result;
import com.mlinyun.onlinecourse.basics.constant.SettingConstant;
import com.mlinyun.onlinecourse.basics.log.LogType;
import com.mlinyun.onlinecourse.basics.log.SystemLog;
import com.mlinyun.onlinecourse.basics.utils.ResultUtil;
import com.mlinyun.onlinecourse.data.entity.Setting;
import com.mlinyun.onlinecourse.data.service.ISettingService;
import com.mlinyun.onlinecourse.data.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Tag(name = "设置接口")
@RequestMapping("/setting")
@CacheConfig(cacheNames = "setting")
@RestController
@Transactional
public class SettingController {

    @Resource
    private ISettingService iSettingService;

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查看单个配置", logType = LogType.DATA_CENTER, doType = "SETTING-01")
    @RequestMapping(value = "/getOne", method = RequestMethod.GET)
    @Operation(summary = "查看单个配置", description = "查看单个配置")
    public Result<Setting> getOne(@RequestParam String id) {
        return new ResultUtil<Setting>().setData(iSettingService.getById(id));
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "修改单个配置", logType = LogType.DATA_CENTER, doType = "SETTING-02")
    @RequestMapping(value = "/setOne", method = RequestMethod.GET)
    @Operation(summary = "修改单个配置", description = "修改单个配置")
    public Result<Object> setOne(@RequestParam String id, @RequestParam String value) {
        Setting setting = iSettingService.getById(id);
        if (setting == null) {
            return ResultUtil.error("不存在");
        }
        if (!Objects.equals(value, setting.getValue())) {
            setting.setValue(value);
            iSettingService.saveOrUpdate(setting);
        }
        return ResultUtil.success();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查看私密配置", logType = LogType.DATA_CENTER, doType = "SETTING-03")
    @Operation(summary = "查看私密配置", description = "查看私密配置")
    @RequestMapping(value = "/seeSecret/{settingName}", method = RequestMethod.GET)
    public Result<Object> seeSecret(@PathVariable String settingName) {
        String result = "";
        Setting setting = iSettingService.getById(settingName);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return ResultUtil.error("配置不存在");
        }
        if (settingName.equals(SettingConstant.ALI_SMS)) {
            result = new Gson().fromJson(setting.getValue(), SmsSettingVO.class).getSecretKey();
        } else if (settingName.equals(SettingConstant.VAPTCHA_SETTING)) {
            result = new Gson().fromJson(setting.getValue(), VaptchaSettingVO.class).getSecretKey();
        }
        return ResultUtil.data(result);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "检查OSS配置", logType = LogType.DATA_CENTER, doType = "SETTING-04")
    @Operation(summary = "检查OSS配置", description = "检查OSS配置")
    @RequestMapping(value = "/oss/check", method = RequestMethod.GET)
    public Result<Object> osscheck() {
        Setting setting = iSettingService.getById(SettingConstant.OSS_USED);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return ResultUtil.error(501, "您还未配置第三方OSS服务");
        }
        return ResultUtil.data(setting.getValue());
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查看OSS配置", logType = LogType.DATA_CENTER, doType = "SETTING-05")
    @Operation(summary = "查看OSS配置", description = "查看OSS配置")
    @RequestMapping(value = "/oss/{serviceName}", method = RequestMethod.GET)
    public Result<OssSettingVO> oss(@PathVariable String serviceName) {
        Setting setting = new Setting();
        if (serviceName.equals(SettingConstant.QINIU_OSS) || serviceName.equals(SettingConstant.ALI_OSS)
                || serviceName.equals(SettingConstant.TENCENT_OSS) || serviceName.equals(SettingConstant.MINIO_OSS)
                || serviceName.equals(SettingConstant.LOCAL_OSS)) {
            setting = iSettingService.getById(serviceName);
        }
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return new ResultUtil<OssSettingVO>().setData(null);
        }
        OssSettingVO ossSettingVO = new Gson().fromJson(setting.getValue(), OssSettingVO.class);
        return new ResultUtil<OssSettingVO>().setData(ossSettingVO);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查看短信配置", logType = LogType.DATA_CENTER, doType = "SETTING-06")
    @Operation(summary = "查看短信配置", description = "查看短信配置")
    @RequestMapping(value = "/sms/{serviceName}", method = RequestMethod.GET)
    public Result<SmsSettingVO> sms(@PathVariable String serviceName) {
        Setting setting = new Setting();
        if (serviceName.equals(SettingConstant.ALI_SMS)) {
            setting = iSettingService.getById(SettingConstant.ALI_SMS);
        }
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return new ResultUtil<SmsSettingVO>().setData(null);
        }
        SmsSettingVO smsSettingVO = new Gson().fromJson(setting.getValue(), SmsSettingVO.class);
        smsSettingVO.setSecretKey("**********");
        if (smsSettingVO.getType() != null) {
            Setting code = new Setting();
            smsSettingVO.setTemplateCode(code.getValue());
        }
        return new ResultUtil<SmsSettingVO>().setData(smsSettingVO);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查看短信模板配置", logType = LogType.DATA_CENTER, doType = "SETTING-07")
    @Operation(summary = "查看短信模板配置", description = "查看短信模板配置")
    @RequestMapping(value = "/sms/templateCode/{type}", method = RequestMethod.GET)
    public Result<String> smsTemplateCode(@PathVariable Integer type) {
        String templateCode = "";
        if (type != null) {
            String template = "CommonUtil.getSmsTemplate(type)";
            Setting setting = iSettingService.getById(template);
            if (StrUtil.isNotBlank(setting.getValue())) {
                templateCode = setting.getValue();
            }
        }
        return new ResultUtil<String>().setData(templateCode);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查看vaptcha配置", logType = LogType.DATA_CENTER, doType = "SETTING-08")
    @Operation(summary = "查看vaptcha配置", description = "查看vaptcha配置")
    @RequestMapping(value = "/vaptcha", method = RequestMethod.GET)
    public Result<VaptchaSettingVO> vaptcha() {
        Setting setting = iSettingService.getById(SettingConstant.VAPTCHA_SETTING);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return new ResultUtil<VaptchaSettingVO>().setData(null);
        }
        VaptchaSettingVO vaptchaSettingVO = new Gson().fromJson(setting.getValue(), VaptchaSettingVO.class);
        vaptchaSettingVO.setSecretKey("**********");
        return new ResultUtil<VaptchaSettingVO>().setData(vaptchaSettingVO);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查看其他配置", logType = LogType.DATA_CENTER, doType = "SETTING-09")
    @Operation(summary = "查看其他配置", description = "查看其他配置")
    @RequestMapping(value = "/other", method = RequestMethod.GET)
    public Result<HttpIpSsoSettingVO> other() {
        Setting setting = iSettingService.getById(SettingConstant.OTHER_SETTING);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return new ResultUtil<HttpIpSsoSettingVO>().setData(null);
        }
        HttpIpSsoSettingVO otherSetting = new Gson().fromJson(setting.getValue(), HttpIpSsoSettingVO.class);
        return new ResultUtil<HttpIpSsoSettingVO>().setData(otherSetting);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "查看公告配置", logType = LogType.DATA_CENTER, doType = "SETTING-10")
    @Operation(summary = "查看公告配置", description = "查看公告配置")
    @RequestMapping(value = "/notice", method = RequestMethod.GET)
    public Result<NoticeSettingVO> notice() {
        Setting setting = iSettingService.getById(SettingConstant.NOTICE_SETTING);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return new ResultUtil<NoticeSettingVO>().setData(null);
        }
        NoticeSettingVO noticeSettingVO = new Gson().fromJson(setting.getValue(), NoticeSettingVO.class);
        return new ResultUtil<NoticeSettingVO>().setData(noticeSettingVO);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "OSS配置", logType = LogType.DATA_CENTER, doType = "SETTING-11")
    @Operation(summary = "OSS配置", description = "OSS配置")
    @RequestMapping(value = "/oss/set", method = RequestMethod.POST)
    public Result<Object> ossSet(OssSettingVO ossSettingVO) {
        String name = ossSettingVO.getServiceName();
        Setting setting = iSettingService.getById(name);
        setting.setValue(new Gson().toJson(ossSettingVO));
        iSettingService.saveOrUpdate(setting);
        Setting used = iSettingService.getById(SettingConstant.OSS_USED);
        used.setValue(name);
        iSettingService.saveOrUpdate(used);
        return ResultUtil.data(null);
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "短信配置", logType = LogType.DATA_CENTER, doType = "SETTING-12")
    @Operation(summary = "短信配置", description = "短信配置")
    @RequestMapping(value = "/sms/set", method = RequestMethod.POST)
    public Result<Object> smsSet(SmsSettingVO smsSettingVO) {
        if (smsSettingVO.getServiceName().equals(SettingConstant.ALI_SMS)) {
            // 阿里
            Setting setting = iSettingService.getById(SettingConstant.ALI_SMS);
            if (StrUtil.isNotBlank(setting.getValue()) && !smsSettingVO.getChanged()) {
                String secrectKey = new Gson().fromJson(setting.getValue(), SmsSettingVO.class).getSecretKey();
                smsSettingVO.setSecretKey(secrectKey);
            }
            if (smsSettingVO.getType() != null) {
                Setting codeSetting = new Setting();
                codeSetting.setValue(smsSettingVO.getTemplateCode());
                iSettingService.saveOrUpdate(codeSetting);
            }
            smsSettingVO.setType(null);
            smsSettingVO.setTemplateCode(null);
            setting.setValue(new Gson().toJson(smsSettingVO));
            iSettingService.saveOrUpdate(setting);

            Setting used = iSettingService.getById(SettingConstant.SMS_USED);
            used.setValue(SettingConstant.ALI_SMS);
            iSettingService.saveOrUpdate(used);
        }
        return ResultUtil.data();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "vaptcha配置", logType = LogType.DATA_CENTER, doType = "SETTING-13")
    @Operation(summary = "vaptcha配置", description = "vaptcha配置")
    @RequestMapping(value = "/vaptcha/set", method = RequestMethod.POST)
    public Result<Object> vaptchaSet(VaptchaSettingVO vaptchaSettingVO) {
        Setting setting = iSettingService.getById(SettingConstant.VAPTCHA_SETTING);
        if (StrUtil.isNotBlank(setting.getValue()) && !vaptchaSettingVO.getChanged()) {
            String key = new Gson().fromJson(setting.getValue(), VaptchaSettingVO.class).getSecretKey();
            vaptchaSettingVO.setSecretKey(key);
        }
        setting.setValue(new Gson().toJson(vaptchaSettingVO));
        iSettingService.saveOrUpdate(setting);
        return ResultUtil.data();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "其他配置", logType = LogType.DATA_CENTER, doType = "SETTING-14")
    @Operation(summary = "其他配置", description = "其他配置")
    @RequestMapping(value = "/other/set", method = RequestMethod.POST)
    public Result<Object> otherSet(HttpIpSsoSettingVO otherSetting) {
        Setting setting = iSettingService.getById(SettingConstant.OTHER_SETTING);
        setting.setValue(new Gson().toJson(otherSetting));
        iSettingService.saveOrUpdate(setting);
        return ResultUtil.data();
    }

    @ApiOperationSupport(author = "LingYun")
    @SystemLog(logName = "通知配置", logType = LogType.DATA_CENTER, doType = "SETTING-15")
    @Operation(summary = "通知配置", description = "通知配置")
    @RequestMapping(value = "/notice/set", method = RequestMethod.POST)
    public Result<Object> noticeSet(NoticeSettingVO noticeSettingVO) {
        Setting setting = iSettingService.getById(SettingConstant.NOTICE_SETTING);
        setting.setValue(new Gson().toJson(noticeSettingVO));
        iSettingService.saveOrUpdate(setting);
        return ResultUtil.data();
    }

}
