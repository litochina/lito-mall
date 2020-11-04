package com.gumoxi.gumoximall.search;

import com.alibaba.fastjson.JSON;
import com.gumoxi.gumoximall.search.config.GumoximallElasticSearchConfiguration;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class GumoximallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @ToString
    @Data
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

    }


    @Test
    public void searchData() throws IOException {


        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_age")
                .field("age");
        searchSourceBuilder.aggregation(aggregation);
        AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg("average_balance")
                .field("balance");
        searchSourceBuilder.aggregation(avgAggregationBuilder);


        searchRequest.source(searchSourceBuilder);



        SearchResponse searchResponse = client.search(searchRequest, GumoximallElasticSearchConfiguration.COMMON_OPTIONS);

        // 分析结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            // do something with the SearchHit
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println("account:" + account);
        }


        Aggregations aggregations = searchResponse.getAggregations();
        Terms byAgeAggregation = aggregations.get("by_age");
        for(Terms.Bucket bucket : byAgeAggregation.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄：" + keyAsString);
        }


        Avg averageBalance = aggregations.get("average_balance");
        System.out.println("平均金额" + averageBalance.getValue());


    }

    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("twitter");
        indexRequest.id("1");

        User user = new User();
        user.setUsername("momo");
        user.setGender("F");
        user.setAge(19);
        String s = JSON.toJSONString(user);
        indexRequest.source(s, XContentType.JSON);

        IndexResponse index = client.index(indexRequest, GumoximallElasticSearchConfiguration.COMMON_OPTIONS);
        System.out.print(index);
    }

    @Data
    class User {
        private String username;
        private  String gender;
        private Integer age;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
