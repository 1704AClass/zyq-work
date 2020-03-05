package com.ningmeng.manage_course.dao;

import com.ningmeng.framework.domain.cms.CmsPage;
import com.ningmeng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

/**
 * Created by 86181 on 2020/2/23.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Tests {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CmsPageClient cmsPageClient;

    @Test
    public void testFeign() {
        //通过服务id调用cms的查询页面接口
        System.out.println("--------------------------");
        CmsPage cmsPage = cmsPageClient.findById("5a92141cb00ffc5a448ff1a0");
        System.out.println("--------------------------"+cmsPage);
    }

    //负载均衡调用
    @Test
    public void testRibbon() {
        //服务id
        String serviceId = "NM-SERVICE-MANAGE-CMS";
        String id = "5a92141cb00ffc5a448ff1a0";
;        for(int i=0;i<10;i++){
            //通过服务id调用
            ResponseEntity<CmsPage> forEntity = restTemplate.getForEntity("http://" + serviceId
                    + "/cms/findById/" + id, CmsPage.class);
            CmsPage cmsPage = forEntity.getBody();
            System.out.println("---------------------------"+cmsPage);
        }
    }

}
