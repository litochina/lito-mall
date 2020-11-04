package com.gumoxi.gumoximall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交的数据
 */
@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType; // 支付方式
    // 无需提交需要购买的商品，去购物车在获取一遍
    //优惠，发票

    private String orderToken;// 防重令牌
    private BigDecimal payPrice; // 应付价格，验价
    private String note;//订单备注

    //用户相关数据，直接去session中去登录的用户

}
