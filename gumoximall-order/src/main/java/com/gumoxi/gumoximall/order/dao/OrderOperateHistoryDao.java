package com.gumoxi.gumoximall.order.dao;

import com.gumoxi.gumoximall.order.entity.OrderOperateHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单操作历史记录
 * 
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 13:41:31
 */
@Mapper
public interface OrderOperateHistoryDao extends BaseMapper<OrderOperateHistoryEntity> {
	
}
