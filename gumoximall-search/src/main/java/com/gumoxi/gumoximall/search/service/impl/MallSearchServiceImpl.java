package com.gumoxi.gumoximall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.gumoxi.gumoximall.common.to.es.SkuEsModel;
import com.gumoxi.gumoximall.common.utils.R;
import com.gumoxi.gumoximall.search.config.GumoximallElasticSearchConfiguration;
import com.gumoxi.gumoximall.search.constant.EsConstant;
import com.gumoxi.gumoximall.search.feign.ProductFeignService;
import com.gumoxi.gumoximall.search.service.MallSearchService;
import com.gumoxi.gumoximall.search.vo.AttrResponseVo;
import com.gumoxi.gumoximall.search.vo.BrandVo;
import com.gumoxi.gumoximall.search.vo.SearchParam;
import com.gumoxi.gumoximall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    // 去es进行检索
    @Override
    public SearchResult search(SearchParam searchParam) {
        // 1、动态构建出需要动态查询的DSL语句
        SearchResult result = null;

        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            // 2、执行检索请求
            SearchResponse response = client.search(searchRequest, GumoximallElasticSearchConfiguration.COMMON_OPTIONS);
            // 3、查询数据封装我们需要的格式
            result = buildSearchResult(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam searchParam) {
        SearchResult result = new SearchResult();
        //1、返回所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> models = new ArrayList<SkuEsModel>();
        for (SearchHit hit: hits.getHits()) {
            String sourceAsString = hit.getSourceAsString();
            SkuEsModel model = JSON.parseObject(sourceAsString, SkuEsModel.class);
            if(!StringUtils.isEmpty(searchParam.getKeyword())) {
                HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                String string = skuTitle.getFragments()[0].string();
                model.setSkuTitle(string);
            }
            models.add(model);
        }
        result.setProducts(models);
        //2、当前所有商品涉及到的属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<SearchResult.AttrVo>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 获取属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            // 得到属性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            // 得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
//        result.setAttrs();
        //2、当前所有商品涉及到的品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<SearchResult.BrandVo>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 得到品牌店id
            long brandId = bucket.getKeyAsNumber().longValue();
            // 得到品牌的名字
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            // 得到品牌的图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        //4、、当前所有商品涉及到的分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<SearchResult.CatalogVo>();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            // 得到分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //5 、分页信息
        result.setPageNum(searchParam.getPageNum());
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        int totalPages = total%EsConstant.PRODUCT_PAGESIZE==0?(int)total/EsConstant.PRODUCT_PAGESIZE:(int)(total/EsConstant.PRODUCT_PAGESIZE+1);
        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<Integer>();
        for(int i = 1; i < totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6、构建面包屑导航功能 属性导航其他的不导航
        if(searchParam.getAttrs() != null && searchParam.getAttrs().size()>0) {
            List<SearchResult.NavVo> collect = searchParam.getAttrs().stream().map(attr -> {
                // 1、分析每个attrs传过来的查询参数值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // attrs=2_5寸;6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if(r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                //2、取消了这个面包屑以后，我们要跳转到哪个地方
                String replace = replaceQueryString(searchParam, attr, "attrs");
                navVo.setLink("http://search.gumoxi.com/list.html?"+replace);

                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);
        }

        //品牌、分类
        if(searchParam.getBrandId()!= null && searchParam.getBrandId().size()>0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            // TODO 远程查询所有品牌
            R r = productFeignService.brandsInfo(searchParam.getBrandId());
            if(r.getCode() == 0) {
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for(BrandVo brandVo:brand) {
                    buffer.append(brandVo.getName() + ";");
                    replace = replaceQueryString(searchParam, brandVo.getBrandId()+"", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gumoxi.com/list.html?"+replace);
            }
            navs.add(navVo);
        }
        // TODO 分类，不需要导航取消


        return result;
    }

    private String replaceQueryString(SearchParam searchParam, String value, String key) {
        String encode  = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return searchParam.get_queryString().replace("&"+key+"=" + encode, "");
    }

    // #模糊匹配，过滤（分类，品牌，属性，价格区间，库存），排序，分页，高亮，聚合分析
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /**
         * 模糊匹配，过滤（分类，品牌，属性，价格区间，库存）
         */

        // 1 构建bool - query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1 must-模糊匹配
        if(!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 1.2 bool - filter 按照三级分类id查询
        if(searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // 1.3 bool - filter 按照品牌id查询
        if(searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        // 1.4 bool - filter 按照是否有库存查询
        if(searchParam.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }
        // 1.5 bool - filter 按照价格区间查询
        if(!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParam.getSkuPrice().split("_");
            if(s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if(s.length == 1) {
                if(searchParam.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if(searchParam.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        // 1.6 bool - filter 按照属性进行过滤
        if(searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            for (String attrStr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }

        sourceBuilder.query(boolQueryBuilder);
        /**
         * 排序，分页，高亮
         */
        //2.1 排序
        if(!StringUtils.isEmpty(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")? SortOrder.ASC: SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        // 2.2 分页
        sourceBuilder.from((searchParam.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 2.3 高亮
        if(!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        /**
         * 聚合分析
         */
        // 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        // 品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        // 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        // 属性聚合
        // 所有的属性
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // 聚合分析当前attr_id对应的名字
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // 聚合分析当前attr_id对应的所有的可能的属性值attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println("构建的DSL" + s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }
}
