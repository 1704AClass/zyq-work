package com.ningmeng.search.service;

import com.ningmeng.framework.domain.course.CoursePub;
import com.ningmeng.framework.domain.search.CourseSearchParam;
import com.ningmeng.framework.model.response.CommonCode;
import com.ningmeng.framework.model.response.QueryResponseResult;
import com.ningmeng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 86181 on 2020/3/2.
 */
@Service
public class EsCourseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsCourseService.class);
    @Value("${ningmeng.elasticsearch.course.index}")
    private String es_index;
    @Value("${ningmeng.elasticsearch.course.type}")
    private String es_type;
    @Value("${ningmeng.elasticsearch.course.source_field}")
    private String source_field;
    @Autowired
    RestHighLevelClient restHighLevelClient;
    public QueryResponseResult list(int page, int size, CourseSearchParam courseSearchParam) {
//设置索引
        SearchRequest searchRequest = new SearchRequest(es_index);
//设置类型
        searchRequest.types(es_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//source源字段过虑
        String[] source_fields = source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields, new String[]{});
//关键字
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
//匹配关键字
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name",
                            "teachplan","description");
            //设置匹配占比
            multiMatchQueryBuilder.minimumShouldMatch("70%");
            //提升另个字段的Boost值
            multiMatchQueryBuilder.field("name",10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //布尔查询
        searchSourceBuilder.query(boolQueryBuilder);
        //请求搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("xuecheng search error..{}",e.getMessage());
            return new QueryResponseResult(CommonCode.SUCCESS,new QueryResult<CoursePub>());
        }
        //结果集处理
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        //记录总数
        long totalHits = hits.getTotalHits();
        //数据列表
        List<CoursePub> list = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            CoursePub coursePub = new CoursePub();
            //取出source
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //取出名称
            String name = (String) sourceAsMap.get("name");
            coursePub.setName(name);
            //图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //价格
            Float price = null;
            try {
                if(sourceAsMap.get("price")!=null ){
                    price = Float.parseFloat((String) sourceAsMap.get("price"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice(price);
            Float price_old = null;
            try {
                if(sourceAsMap.get("price_old")!=null ){
                    price_old = Float.parseFloat((String) sourceAsMap.get("price_old"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice_old(price_old);
            list.add(coursePub);
        }
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(totalHits);
        QueryResponseResult coursePubQueryResponseResult = new
                QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return coursePubQueryResponseResult;
    }
}
