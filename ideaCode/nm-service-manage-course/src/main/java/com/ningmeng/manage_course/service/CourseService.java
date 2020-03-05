package com.ningmeng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ningmeng.framework.domain.cms.CmsPage;
import com.ningmeng.framework.domain.cms.response.CmsPageResult;
import com.ningmeng.framework.domain.course.*;
import com.ningmeng.framework.domain.course.ext.CourseInfo;
import com.ningmeng.framework.domain.course.ext.TeachplanNode;
import com.ningmeng.framework.domain.course.response.CourseCode;
import com.ningmeng.framework.domain.course.response.CoursePublishResult;
import com.ningmeng.framework.domain.course.response.CourseView;
import com.ningmeng.framework.exception.CustomExceptionCast;
import com.ningmeng.framework.model.response.CommonCode;
import com.ningmeng.framework.model.response.QueryResponseResult;
import com.ningmeng.framework.model.response.ResponseResult;
import com.ningmeng.manage_course.client.CmsPageClient;
import com.ningmeng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by wangb on 2020/2/18.
 */
@Service
public class CourseService {


    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    @Autowired
    TeachplanMapper teachplanMapper;
    //课程
    @Autowired
    CourseMapper courseMapper;
    //课程计划
    @Autowired
    TeachplanRepository teachplanRepository;
    //课程基本信息
    @Autowired
    CourseBaseRepository courseBaseRepository;
    //图片
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    CmsPageClient cmsPageClient;


    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        CustomExceptionCast.cast(CourseCode.COURSE_PUBLISH_VIEWERROR);
        return null;
    }

    //课程预览
    public CoursePublishResult preview(String courseId){
        CourseBase one = this.findCourseBaseById(courseId);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId+".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);
        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.add(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        System.out.println("[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[["+pageId);
        String pageObject = cmsPageResult.getMessage();
        CmsPage cmsPage1 = JSON.parseObject(pageObject, cmsPage.getClass());
        if(cmsPage1==null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面url
        String pageUrl = previewUrl+cmsPage1.getPageId();

        System.out.println("==============================="+pageUrl);

        //创建课程索引
        //创建课程索引信息
        CoursePub coursePub = createCoursePub(courseId);
        //向数据库保存课程索引信息
        CoursePub newCoursePub = saveCoursePub(courseId, coursePub);
        if(newCoursePub==null){
        //创建课程索引信息失败
            CustomExceptionCast.cast(CourseCode.COURSE_PUBLISH_CDETAILERROR);
        }

        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }


    /**
     *
     * @param id 课程ID
     * @return
     */
    public CourseView getCoruseView(String id){


        CourseView courseView = new CourseView();

        //查询课程基本信息
        Optional<CourseBase> byId = courseBaseRepository.findById(id);
        if(byId.isPresent()){
            courseView.setCourseBase(byId.get());
        }
        //查询课程营销信息
        Optional<CourseMarket> byId1 = courseMarketRepository.findById(id);
        if(byId1.isPresent()){
            courseView.setCourseMarket(byId1.get());
        }
        //查询课程图片信息
        Optional<CoursePic> byId2 = coursePicRepository.findById(id);
        if(byId2.isPresent()){
            courseView.setCoursePic(byId2.get());
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        if(teachplanNode != null){
            courseView.setTeachplanNode(teachplanNode);
        }

        return  courseView;
    }

    public CoursePic findCoursepic(String courseId) {

        return coursePicRepository.findById(courseId).get();
    }

    //添加课程图片
    @Transactional
    public ResponseResult saveCoursePic(String courseId,String pic){
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if(picOptional.isPresent()){
            coursePic = picOptional.get();
        }
        //没有课程图片则新建对象
        if(coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        //保存课程图片
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 根据公司ID查询课程列表
     * springboot自动加入事务管理
     * @param
     * @return
     */
    @Transactional
    public QueryResponseResult findCourseListPage(int page,int pagesize,String companyId){
        if(companyId ==null || "".equals(companyId)){
            //通用异常处理 都是父子级关系
            CustomExceptionCast.cast(CommonCode.FAIL);
        }
        PageHelper.startPage(page,pagesize);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(companyId);
        com.ningmeng.framework.model.response.QueryResult queryResult = new com.ningmeng.framework.model.response.QueryResult();
        queryResult.setList(courseListPage.getResult());
        queryResult.setTotal(courseListPage.getTotal());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }
    //获取课程根结点，如果没有则添加根结点
    public String getTeachplanRoot(String courseId){
        //校验课程id
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }
        CourseBase courseBase = optional.get();
        //取出课程计划根结点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId,"0");
        if(teachplanList == null || teachplanList.size()==0){
            //新增一个根结点
            Teachplan teachplanRoot = new Teachplan();
            teachplanRoot.setCourseid(courseId);
            teachplanRoot.setPname(courseBase.getName());
            teachplanRoot.setParentid("0");
            teachplanRoot.setGrade("1");//1级
            teachplanRoot.setStatus("0");//未发布
            teachplanRepository.save(teachplanRoot);
            return teachplanRoot.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }
    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
        //校验课程id和课程计划名称
        if(teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())){
            CustomExceptionCast.cast(CommonCode.FAIL);
        }
        //取出课程id
        String courseid = teachplan.getCourseid();
        //取出父结点id
        String parentid = teachplan.getParentid();
        if(StringUtils.isEmpty(parentid)){
            //如果父结点为空则获取根结点
            parentid= getTeachplanRoot(courseid);
        }
        //取出父结点信息
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(parentid);
        if(!teachplanOptional.isPresent()){
            CustomExceptionCast.cast(CommonCode.FAIL);
        }
        //父结点
        Teachplan teachplanParent = teachplanOptional.get();
        //父结点级别
        String parentGrade = teachplanParent.getGrade();
        //设置父结点
        teachplan.setParentid(parentid);
        teachplan.setStatus("0");//未发布
        //子结点的级别，根据父结点来判断
        if(parentGrade.equals("1")){
            teachplan.setGrade("2");
        }else if(parentGrade.equals("2")){
            teachplan.setGrade("3");
        }
        //设置课程id
        teachplan.setCourseid(teachplanParent.getCourseid());
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //保存CoursePub
    public CoursePub saveCoursePub(String id, CoursePub coursePub){
        if(StringUtils.isNotEmpty(id)){
            CustomExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if(coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }
        if(coursePubNew == null){
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub,coursePubNew);
        //设置主键
        coursePubNew.setId(id);
        //更新时间戳为最新时间
        coursePub.setTimestamp(new Date());
        //发布时间修改课程发布方法，添加调用saveCoursePub方法的代码，添加部分的代码如下：
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePub.setPubTime(date);
        coursePubRepository.save(coursePub);
        return coursePub;
    }

    //创建coursePub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);
        //基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional == null){
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //将课程计划转成json
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);
        return coursePub;
    }


}
