package com.mlinyun.onlinecourse.data.utils.file;

import com.mlinyun.onlinecourse.data.vo.OssSettingVO;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 本地文件管理接口
 */
public interface FileManage {

    @Schema(description = "删除文件")
    void deleteFile(String key);

    @Schema(description = "重命名文件")
    String renameFile(String fromKey, String toKey);

    @Schema(description = "获取配置")
    OssSettingVO getOssSetting();

    @Schema(description = "拷贝文件")
    String copyFile(String fromKey, String toKey);

    @Schema(description = "文件流上传")
    String inputStreamUpload(InputStream inputStream, String key, MultipartFile file);

}

