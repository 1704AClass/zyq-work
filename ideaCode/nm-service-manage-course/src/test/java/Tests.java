import com.netflix.discovery.converters.Auto;
import com.ningmeng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

/**
 * Created by 12699 on 2020/2/23.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Tests {
    @Auto
    private RestTemplate restTemplate;
    //负载均衡调用
    @Test
    public void testRibbon() {
        //服务id
        String serviceId = "NM‐SERVICE‐MANAGE‐CMS";
        for(int i=0;i<10;i++){
            //通过服务id调用
            ResponseEntity<CmsPage> forEntity = restTemplate.getForEntity("http://" + serviceId
                    + "/cms/page/get/5a754adf6abb500ad05688d9", CmsPage.class);
            CmsPage cmsPage = forEntity.getBody();
            System.out.println(cmsPage);
        }
    }
}
