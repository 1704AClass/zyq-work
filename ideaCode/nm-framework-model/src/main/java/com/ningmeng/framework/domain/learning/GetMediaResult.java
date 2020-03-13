package com.ningmeng.framework.domain.learning;

import com.ningmeng.framework.model.response.ResponseResult;
import com.ningmeng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by 86181 on 2020/3/9.
 */
@Data
@ToString
@NoArgsConstructor
public class GetMediaResult extends ResponseResult{

    public GetMediaResult(ResultCode resultCode, String fileUrl) {
        super(resultCode);
        this.fileUrl = fileUrl;
    }
    //媒资文件播放地址
    private String fileUrl;

}
