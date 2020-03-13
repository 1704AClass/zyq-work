package com.ningmeng.manage_course.servic;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ningmeng.framework.domain.cms.CmsPage;
import com.ningmeng.framework.domain.cms.response.CmsPostPageResult;
import com.ningmeng.framework.domain.course.*;
import com.ningmeng.framework.domain.course.ext.CourseInfo;
import com.ningmeng.framework.domain.course.ext.CourseView;
import com.ningmeng.framework.domain.course.ext.TeachplanNode;
import com.ningmeng.framework.domain.course.response.AddCourseResult;
import com.ningmeng.framework.domain.course.response.CourseCode;
import com.ningmeng.framework.domain.course.response.CoursePublishResult;
import com.ningmeng.framework.exception.CustomExceptionCast;
import com.ningmeng.framework.model.response.CommonCode;
import com.ningmeng.framework.model.response.QueryResponseResult;
import com.ningmeng.framework.model.response.QueryResult;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by 12699 on 2020/2/19.
 */
@Service
public class CourseService {
    @Autowired
    private CoursePubRepository coursePubRepository;
    @Autowired
    private CoursePicRepository coursePicRepository;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    private CmsPageClient cmsPageClient;
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
    //保存媒资信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null){
            CustomExceptionCast.cast(CourseCode.COURSE_MEDIS_NAMEISNULL);
        }
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        if("".equals(teachplanId)||teachplanId==null){
            CustomExceptionCast.cast(CourseCode.COURSE_MEDIS_NAMEISNULL);
        }
        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            CustomExceptionCast.cast(CourseCode.COURSE_MEDIS_NAMEISNULL);
        }
        if(!optional.get().getGrade().equals("3")){
            CustomExceptionCast.cast(CourseCode.COURSE_MEDIS_NAMEISNULL);
        }
        TeachplanMedia teachplanMedia1=null;
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        if(!teachplanMediaOptional.isPresent()){
            teachplanMedia1 = new TeachplanMedia();
        }else{
            teachplanMedia1 = teachplanMediaOptional.get();
        }
        //保存媒资信息与课程计划信息
        teachplanMedia1.setTeachplanId(teachplanId);
        teachplanMedia1.setCourseId(teachplanMedia.getCourseId());
        teachplanMedia1.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        teachplanMedia1.setMediaId(teachplanMedia.getMediaId());
        teachplanMedia1.setMediaUrl(teachplanMedia.getMediaUrl());
        teachplanMediaRepository.save(teachplanMedia1);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //课程发布
    @Transactional
    public CoursePublishResult publish(String courseId){
        //课程信息
        CourseBase one = this.findCourseBaseById(courseId);
        //发布课程详情页面
        CmsPostPageResult cmsPostPageResult = publish_page(courseId);
        if(!cmsPostPageResult.isSuccess()){
            CustomExceptionCast.cast(CommonCode.FAIL);
        }
        //更新课程状态
        CourseBase courseBase = saveCoursePubState(courseId);
        //课程索引....
        //创建课程索引
        // 创建课程索引信息
        CoursePub coursePub = createCoursePub(courseId);
        //向数据库保存课程索引信息
        CoursePub newCoursePub = saveCoursePub(courseId, coursePub);
        //保存课程计划媒资信息到待索引表
        this.saveTeachplanMediaPub(courseId);
        if(newCoursePub==null){
            //创建课程索引信息失败
            CustomExceptionCast.cast(CourseCode.COURSE_PUBLISH_CDETAILERROR);
        }
        // 课程缓存...

        // 页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }
    //更新课程发布状态
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }
    //发布课程正式页面
    public CmsPostPageResult publish_page(String courseId){
        CourseBase one = this.findCourseBaseById(courseId);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);
        //课程预览站点
        // 模板
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
        //发布页面
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        return cmsPostPageResult;
    }
    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        CustomExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        return null;
    }
    public CoursePublishResult preview(String courseId){
        CourseBase one = this.findCourseBaseById(courseId);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        // 模板
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
        ResponseResult responseResult = cmsPageClient.add(cmsPage);
        if(!responseResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面id
        String message = responseResult.getMessage();
        CmsPage cmsPage1 = JSON.parseObject(message, CmsPage.class);
        if(cmsPage1==null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面url
        String pageUrl = previewUrl+cmsPage1.getPageId();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }




    @Transactional
    public QueryResponseResult findCourseListPage(int page,int pageSize,String companyId){
        if(companyId==null||"".equals(companyId)){
            CustomExceptionCast.cast(CommonCode.FAIL);
        }
        PageHelper.startPage(1, 10);//查询第一页，每页显示10条记录
        Page<CourseInfo> pageAll= courseMapper.findCourseListPage(companyId);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(pageAll.getResult());
        queryResult.setTotal(pageAll.getTotal());
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
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
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
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
        }Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }
    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
        //校验课程id和课程计划名称
        if(teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) || StringUtils.isEmpty(teachplan.getPname())){
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
        // 子结点的级别，根据父结点来判断
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
    //添加课程提交
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }
    //修改
    @Transactional
    public ResponseResult updateCourseBase(String id,CourseBase courseBase){
        CourseBase courseBaseById = this.getCourseBaseById(id);
        if (courseBaseById==null){
            //抛出异常
            CustomExceptionCast.cast(CommonCode.FAIL);
        }
        //修改参数
        courseBaseById.setName(courseBase.getName());
        courseBaseById.setMt(courseBase.getMt());
        courseBaseById.setSt(courseBase.getSt());
        courseBaseById.setGrade(courseBase.getGrade());
        courseBaseById.setStudymodel(courseBase.getStudymodel());
        courseBaseById.setUsers(courseBase.getUsers());
        courseBaseById.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(courseBaseById);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    public CourseMarket getCourseMarketById(String courseid){
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseid);
        if (!optional.isPresent()){
            return optional.get();
        }
        return null;
    }
    @Transactional
    public CourseMarket updateCourseMarket(String id,CourseMarket courseMarket){
        CourseMarket courseMarketById = this.getCourseMarketById(id);
        if(courseMarketById!=null){
            courseMarketById.setCharge(courseMarket.getCharge());
            courseMarketById.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            courseMarketById.setEndTime(courseMarket.getEndTime());//课程有效期，结束时
            courseMarketById.setPrice(courseMarket.getPrice());
            courseMarketById.setQq(courseMarket.getQq());
            courseMarketById.setValid(courseMarket.getValid());
            courseMarketRepository.save(courseMarketById);
        }else{
            //添加课程营销信息
            courseMarketById = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, courseMarketById);
            //设置课程id
            courseMarketById.setId(id);
            courseMarketRepository.save(courseMarketById);
        }
        return courseMarketById;
    }
    //添加课程图片
    @Transactional
    public ResponseResult saveCoursePic(String courseId,String pic){
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if(picOptional.isPresent()){
            coursePic = picOptional.get();
        }//没有课程图片则新建对象
        if(coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        //保存课程图片
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    public CoursePic findCoursepic(String courseId) {
        return coursePicRepository.findById(courseId).get();
    }
    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //执行删除，返回1表示删除成功，返回0表示删除失败
        long result = coursePicRepository.deleteByCourseid(courseId);
        if(result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }
    //课程视图查询
    public CourseView getCoruseView(String id) {
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(picOptional.get());
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
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
        //发布时间
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
        coursePub.setTeachplan(teachplanString); return coursePub;
    }
    //保存课程计划媒资信息
    private void saveTeachplanMediaPub(String courseId){
        //将课程计划媒资信息存储待索引表
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for(TeachplanMedia teachplanMedia:teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub =new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }
}

