package com.ningmeng.manage_course.controller;

import com.ningmeng.api.courseapi.CourseControllerApi;
import com.ningmeng.framework.domain.course.*;
import com.ningmeng.framework.domain.course.ext.CourseView;
import com.ningmeng.framework.domain.course.ext.TeachplanNode;
import com.ningmeng.framework.domain.course.request.CourseListRequest;
import com.ningmeng.framework.domain.course.response.AddCourseResult;
import com.ningmeng.framework.domain.course.response.CoursePublishResult;
import com.ningmeng.framework.model.response.CommonCode;
import com.ningmeng.framework.model.response.QueryResponseResult;
import com.ningmeng.framework.model.response.ResponseResult;
import com.ningmeng.manage_course.servic.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Created by 12699 on 2020/2/19.
 */
@RestController
@RequestMapping("/course")
public class CourseController  implements CourseControllerApi {
    @Autowired
    CourseService courseService;
    @Override
    @PostMapping("/savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemedia(teachplanMedia);
    }
    //查询课程计划
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(String courseId) {
        return courseService.findTeachplanList(courseId);
    }
    //添加课程计划
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    @Override
    @PreAuthorize("hasAuthority('course_find_list')")
    @GetMapping("/course/findCourseList/{page}/{size}")
    public QueryResponseResult findCourseList(@PathVariable("page") int page, @PathVariable("size") int size, CourseListRequest courseListRequest) {
        return courseService.findCourseListPage(page,size,courseListRequest);
    }

    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    @Override
    @PreAuthorize("hasAuthority('course_get_baseinfo')")
    @GetMapping("/coursebase/get/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) throws RuntimeException {
        return courseService.getCourseBaseById(courseId);
    }

    @Override
    @PutMapping("/coursebase/update/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id, @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBase(id,courseBase);
    }

    @Override
    @GetMapping("/courseMarket/get/{courseId}")
    public CourseMarket getCourseMarketById(String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    @Override
    @PostMapping("/courseMarket/uodate/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id")String id,@RequestBody CourseMarket courseMarket) {
        CourseMarket courseMarket1 = courseService.updateCourseMarket(id, courseMarket);
        if (courseMarket1!=null){
            return new ResponseResult(CommonCode.SUCCESS);
        }else{
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId")String courseId, @RequestParam("pic")String pic) {
        //保存课程图片
        return courseService.saveCoursePic(courseId, pic);
    }

    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId")String courseId) {
        return courseService.findCoursepic(courseId);
    }
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }
    @Override
    @PreAuthorize("hasAuthority('course_find_view')")
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.getCoruseView(id);
    }
    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    @Override @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable String id) {
        return courseService.publish(id);
    }
}
