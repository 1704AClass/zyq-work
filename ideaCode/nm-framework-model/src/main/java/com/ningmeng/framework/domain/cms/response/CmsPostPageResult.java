package com.ningmeng.framework.domain.cms.response;

import com.ningmeng.framework.model.response.ResponseResult;
import com.ningmeng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 12699 on 2020/2/26.
 */
@Data
@NoArgsConstructor//无参构造器注解
public class CmsPostPageResult extends ResponseResult {
    String pageUrl; public CmsPostPageResult(ResultCode resultCode, String pageUrl) {
        super(resultCode);
        this.pageUrl = pageUrl;
    }
}
