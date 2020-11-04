package com.gumoxi.gumoximall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumoxi.gumoximall.common.to.SkuReductionTo;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 14:13:55
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

