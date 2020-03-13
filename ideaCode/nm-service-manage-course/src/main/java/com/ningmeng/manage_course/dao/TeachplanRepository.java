package com.ningmeng.manage_course.dao;

import com.ningmeng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by 12699 on 2020/2/19.
 */
public interface TeachplanRepository extends JpaRepository<Teachplan, String> {
    //定义方法根据课程id和父结点id查询出结点列表，可以使用此方法实现查询根结点
    public List<Teachplan> findByCourseidAndParentid(String courseId, String parentId);
}
