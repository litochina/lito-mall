package com.gumoxi.gumoximall.seckill.to;

import com.gumoxi.gumoximall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRedisTo {

    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    //sku的详细信息
    private SkuInfoVo skuInfo;

    /**
     * 当前商品的秒杀开始时间
     */
    private Long startTime;

    /**
     * 当前商品的秒杀结束时间
     */
    private Long endTime;

    private String randomCode;
}
