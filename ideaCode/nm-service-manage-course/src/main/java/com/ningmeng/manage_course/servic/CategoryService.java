package com.ningmeng.manage_course.servic;

import com.ningmeng.framework.domain.course.ext.CategoryNode;
import com.ningmeng.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 12699 on 2020/2/20.
 */
@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    //查询分类
    public CategoryNode findList(){
        return categoryMapper.selectList();
    }
}
