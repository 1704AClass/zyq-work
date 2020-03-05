package com.ningmeng.api.filesystemapi;

import com.ningmeng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by 86181 on 2020/2/21.
 */
@Api(value = "文件系统服务接口",description = "提供文件系统服务接口常规操作")
public interface FileSystemControllerApi {
    @ApiOperation(value = "文件上传接口")
    public UploadFileResult upload(MultipartFile file,
                                   String filetag,
                                   String businesskey,
                                   String metadata);
}
