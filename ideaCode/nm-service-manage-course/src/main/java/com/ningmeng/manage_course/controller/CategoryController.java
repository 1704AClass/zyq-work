package com.ningmeng.manage_course.controller;

import com.ningmeng.api.course.CategoryControllerApi;
import com.ningmeng.framework.domain.course.ext.CategoryNode;
import com.ningmeng.manage_course.servic.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by 12699 on 2020/2/20.
 */
public class CategoryController implements CategoryControllerApi {

    @Autowired
    CategoryService categoryService;
    @Override
    @GetMapping("list")
    public CategoryNode findList() {
        return categoryService.findList();

    }
}
