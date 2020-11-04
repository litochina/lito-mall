package com.gumoxi.gumoximall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的检索参数
 */
@Data
public class SearchParam {

    private String keyword;//页面传递过来全文匹配的
    private Long catalog3Id;

    /**
     * sort=saleCount_desc/asc
     * sort=skuPrice_desc/asc
     * sort=hotScore_desc/asc
     */
    private String sort;

    /**
     * 好多的过滤条件
     * hasStock、skuPrice、brandId、catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     * brandId=1
     * attrs=2_5寸;6寸&attrs=1_8G;16G
     *
     */
    private Integer hasStock;// 是否只显示有货
    private String skuPrice;// 价格区间查询
    private List<Long> brandId;//按照品牌进行查询，可以多选
    private List<String> attrs;// 按照属性进行筛选
    private Integer pageNum = 1;//查询页码

    private String _queryString;

}
