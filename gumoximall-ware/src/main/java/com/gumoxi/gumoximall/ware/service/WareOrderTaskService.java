package com.gumoxi.gumoximall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-20 11:01:44
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn);
}

