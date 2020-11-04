package com.gumoxi.gumoximall.order.dao;

import com.gumoxi.gumoximall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 13:41:31
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
