package com.ningmeng.learning.dao;

import com.ningmeng.framework.domain.learning.NmLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by 86181 on 2020/3/17.
 */
public interface NmLearningCourseRepository extends JpaRepository<NmLearningCourse, String> {
    //根据用户和课程查询选课记录，用于判断是否添加选课
    NmLearningCourse findNmLearningCourseByUserIdAndCourseId(String userId, String courseId);
}
