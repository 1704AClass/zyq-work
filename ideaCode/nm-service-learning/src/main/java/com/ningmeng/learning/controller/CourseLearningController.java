package com.ningmeng.learning.controller;

import com.ningmeng.api.learningapi.CourseLearningControllerApi;
import com.ningmeng.framework.domain.learning.response.GetMediaResult;
import com.ningmeng.learning.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by 12699 on 2020/3/9.
 */
public class CourseLearningController implements CourseLearningControllerApi {
    @Autowired
    LearningService learningService;
    @Override
    @GetMapping("/getmedia/{courseId}/{teachplanId}")
    public GetMediaResult getmedia(@PathVariable String courseId, @PathVariable String teachplanId) {
        //获取课程学习地址
        return learningService.getMedia(courseId, teachplanId);
    }
}
