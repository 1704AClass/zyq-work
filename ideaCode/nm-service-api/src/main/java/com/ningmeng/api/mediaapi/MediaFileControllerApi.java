package com.ningmeng.api.mediaapi;

import com.ningmeng.framework.domain.media.request.QueryMediaFileRequest;
import com.ningmeng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by 12699 on 2020/3/6.
 */
@Api(value = "媒体文件管理",description = "媒体文件管理接口",tags = {"媒体文件管理接口"})
public interface MediaFileControllerApi {
    @ApiOperation("查询文件列表")
    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);
}
