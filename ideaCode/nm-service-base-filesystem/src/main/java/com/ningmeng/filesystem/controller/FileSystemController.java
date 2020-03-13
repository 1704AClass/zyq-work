package com.ningmeng.filesystem.controller;

import com.ningmeng.api.filesystemapi.FileSystemControllerApi;
import com.ningmeng.filesystem.service.FileSystemService;
import com.ningmeng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by 12699 on 2020/2/21.
 */
@RestController
@RequestMapping("/filesystem")
public class FileSystemController  implements FileSystemControllerApi {
    @Autowired
    FileSystemService fileSystemService;
    @Override
    @PostMapping("/upload")
    public UploadFileResult upload(@RequestParam("multipartFile")MultipartFile multipartFile, @RequestParam(value = "filetag", required = true)String filetag, @RequestParam(value = "businesskey", required = false)String businesskey, @RequestParam(value = "metedata", required = false)String metadata) {
        return fileSystemService.upload(multipartFile,filetag,businesskey,metadata);
    }
}
