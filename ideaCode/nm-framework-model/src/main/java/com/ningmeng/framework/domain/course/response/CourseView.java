package com.ningmeng.framework.domain.course.response;

import com.ningmeng.framework.domain.course.CourseBase;
import com.ningmeng.framework.domain.course.CourseMarket;
import com.ningmeng.framework.domain.course.CoursePic;
import com.ningmeng.framework.domain.course.ext.TeachplanNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by 86181 on 2020/2/24.
 * 组装模板页面需要的数据接口
 * 仅供课程静态化程序调用使用
 */
@Data
@ToString
@NoArgsConstructor
public class CourseView implements Serializable{
    CourseBase courseBase;//基础信息
    CourseMarket courseMarket;//课程营销
    CoursePic coursePic;//课程图片
    TeachplanNode teachplanNode;//教学计划
}
