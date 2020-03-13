package com.ningmeng.manage_course.dao;

import com.ningmeng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by 12699 on 2020/2/19.
 */
@Mapper
public interface TeachplanMapper {
    public TeachplanNode selectList(String courseId);
}
