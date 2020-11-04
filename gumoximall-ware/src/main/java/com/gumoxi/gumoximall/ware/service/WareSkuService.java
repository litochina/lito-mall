package com.gumoxi.gumoximall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumoxi.gumoximall.common.to.mq.OrderTo;
import com.gumoxi.gumoximall.common.to.mq.StockLockedTo;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.to.SkuHasStockTo;
import com.gumoxi.gumoximall.ware.entity.WareSkuEntity;
import com.gumoxi.gumoximall.ware.vo.LockStockResult;
import com.gumoxi.gumoximall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-20 11:01:44
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);

    void unlockStock(OrderTo to);
}

