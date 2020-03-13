package com.ningmeng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by 86181 on 2020/2/26.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchDemo {

    @Autowired
    RestHighLevelClient client;

    @Test
    public void testAddDoc() throws IOException {
        //准备json数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);
        jsonMap.put("pic", "group1/M00/00/00/wKhlgV5NtNOANpVvAATLUBCpRoA317.jpg");
        //索引请求对象 第一个索引库名称 第二个类型（表名）
        IndexRequest indexRequest = new IndexRequest("nm_course","doc");
        //指定索引文档内容
        indexRequest.source(jsonMap);
        //索引响应对象
        IndexResponse indexResponse = client.index(indexRequest);
        //获取响应结果
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println("--------------------------------------"+result);
    }

    //查询文档
    @Test
    public void getDoc() throws IOException {
        GetRequest getRequest = new GetRequest(
                "nm_course",
                "doc",
                "1");
        GetResponse getResponse = client.get(getRequest);
        boolean exists = getResponse.isExists();
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    //更新文档
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("nm_course", "doc",
                "2");
        Map<String, String> map = new HashMap<>();
        map.put("name", "spring cloud实战");
        updateRequest.doc(map);
        UpdateResponse update = client.update(updateRequest);
        RestStatus status = update.status();
        System.out.println(status);
    }

    //根据id删除文档
    @Test
    public void testDelDoc() throws IOException {
        //删除文档id
        String id = "2";
        //删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest("nm_course","doc",id);
        //响应对象
        DeleteResponse deleteResponse = client.delete(deleteRequest);
        //获取响应结果
        DocWriteResponse.Result result = deleteResponse.getResult();
        System.out.println(result);
    }
    //分页查询
    @Test
    public void testESLPage() throws IOException{
        SearchRequest searchRequest = new SearchRequest("nm_course");
        searchRequest.types("nm_course");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //分页查询，设置起始下标，从0开始
        searchSourceBuilder.from(0);
        //每页显示个数
        searchSourceBuilder.size(10);
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
    }
    @Test
    public void testTermESL()throws IOException{
        SearchRequest searchRequest = new SearchRequest("nm_course");
        searchRequest.types("nm_course");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
    }
    @Test
    public void testIdESL()throws IOException{
        SearchRequest searchRequest = new SearchRequest("nm_course");
        searchRequest.types("nm_course");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        String[] split = new String[]{"1","2"};
        List<String> idList = Arrays.asList(split);
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id", idList));
    }
    //根据关键字搜索
    @Test
    public void testMatchQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("nm_course");
        searchRequest.types("nm_course");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        //匹配关键字
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开 发").operator(Operator.OR));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }
    //根据关键字搜索
    @Test
    public void testMultiQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("nm_course");
        searchRequest.types("nm_course");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        //匹配关键字
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开 发").operator(Operator.OR));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架", "name", "description") .minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name",10);//提升boost
    }
    //BoolQuery，将搜索关键字分词，拿分词去索引库搜索
    @Test
    public void testBoolQuery() throws IOException {
        //创建搜索请求对象
        SearchRequest searchRequest= new SearchRequest("nm_course");
        searchRequest.types("doc");
        //创建搜索源配置对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name","pic","studymodel"},new String[]{});
        //multiQuery
        String keyword = "spring开发框架";
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架",
                "name", "description")
                .minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name",10);
        //TermQuery
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
        //设置布尔查询对象
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);//设置搜索源配置
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for(SearchHit hit:searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap);
        }
    }
    //布尔查询使用过虑器
    @Test
    public void testFilter() throws IOException {
        SearchRequest searchRequest = new SearchRequest("nm_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "description"},
                new String[]{});
        searchRequest.source(searchSourceBuilder);
        //匹配关键字
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框 架", "name", "description");
        //设置匹配占比
        multiMatchQueryBuilder.minimumShouldMatch("50%");
        //提升另个字段的Boost值
        multiMatchQueryBuilder.field("name", 10);
        searchSourceBuilder.query(multiMatchQueryBuilder);
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(searchSourceBuilder.query());
        //过虑
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }
    @Test
    public void testSort() throws IOException {
        SearchRequest searchRequest = new SearchRequest("nm_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","description"},
                new String[]{});
        searchRequest.source(searchSourceBuilder);
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //过虑
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //排序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }
    @Test
    public void testHighlight() throws IOException {
        SearchRequest searchRequest = new SearchRequest("nm_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","description"},
                new String[]{});
        searchRequest.source(searchSourceBuilder);
        //匹配关键字
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发",
                "name", "description");
        searchSourceBuilder.query(multiMatchQueryBuilder);
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(searchSourceBuilder.query());
        //过虑
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //排序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));
        //高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");//设置前缀
        highlightBuilder.postTags("</tag>");//设置后缀
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        // highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //名称
            String name = (String) sourceAsMap.get("name");
            //取出高亮字段内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields!=null){
                HighlightField nameField = highlightFields.get("name");
                if(nameField!=null){
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    }
                    name = stringBuffer.toString();
                }
            }
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }
}
