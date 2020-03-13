package com.ningmeng.manage_cms.controller;

import com.ningmeng.api.cmsapi.CmsPageControllerApi;
import com.ningmeng.framework.domain.cms.CmsPage;
import com.ningmeng.framework.domain.cms.request.QueryPageRequest;
import com.ningmeng.framework.domain.cms.response.CmsPageResult;
import com.ningmeng.framework.domain.cms.response.CmsPostPageResult;
import com.ningmeng.framework.model.response.QueryResponseResult;
import com.ningmeng.framework.model.response.ResponseResult;
import com.ningmeng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by 12699 on 2020/2/11.
 */
@RestController
@RequestMapping("/cms")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    PageService pageService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {
        //暂时采用测试数据，测试接口是否可以正常运行
        return pageService.findList(page,size,queryPageRequest);
    }

    //添加页面
    @Override
    @PostMapping("/add")
    public ResponseResult add(@RequestBody CmsPage cmsPage) {
        return pageService.add(cmsPage);
    }

    @Override
    @GetMapping("/get/{id}")
    public CmsPage findById(@PathVariable("id") String id) {
        return pageService.getById(id);
    }

    @Override
    @PutMapping("/update/{id}")
    //这里使用put方法，http 方法中put表示更新
    public CmsPageResult update(@PathVariable("id") String id, @RequestBody CmsPage cmsPage) {
        return pageService.update(id,cmsPage);
    }
    @Override
    @DeleteMapping("/delete/{id}") //使用http的delete方法完成岗位操作
    public ResponseResult delete(String id) {
        return pageService.delete(id);
    }

    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(String pageId) {
        return pageService.postPage(pageId);
    }

    @Override
    @PostMapping("/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return pageService.postPageQuick(cmsPage);
    }

    @GetMapping("/preview/{id}")
    public String preview(@PathVariable("id") String cmsPageId){
        try {
            String pageHtml = pageService.preview(cmsPageId);
        }catch (Exception e){
            System.out.println("失败");
        }
        return pageService.preview(cmsPageId);
    }

}
