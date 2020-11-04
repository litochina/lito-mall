package com.gumoxi.gumoximall.seckill.service;

import com.gumoxi.gumoximall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {

    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
