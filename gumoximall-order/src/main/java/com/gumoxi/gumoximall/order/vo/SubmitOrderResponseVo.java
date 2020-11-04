package com.gumoxi.gumoximall.order.vo;

import com.gumoxi.gumoximall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;
}
