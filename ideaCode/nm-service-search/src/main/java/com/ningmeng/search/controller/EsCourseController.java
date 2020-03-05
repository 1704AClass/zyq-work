package com.ningmeng.search.controller;

import com.ningmeng.framework.domain.search.CourseSearchParam;
import com.ningmeng.framework.model.response.QueryResponseResult;
import com.ningmeng.search.service.EsCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by 86181 on 2020/3/2.
 */
@RequestMapping("/search/course")
@RestController
public class EsCourseController {
    @Autowired
    EsCourseService esCourseService;

    @GetMapping(value="/list/{page}/{size}")
    public QueryResponseResult list(@PathVariable("page") int page,
                                               @PathVariable("size") int size, CourseSearchParam courseSearchParam) throws IOException {
        return esCourseService.list(page,size,courseSearchParam);
    }
}
