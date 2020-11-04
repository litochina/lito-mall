package com.gumoxi.gumoximall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    // 收货地址 mms_member_receive_address表
    @Getter @Setter
    List<MemberAddressVo> address;

    // 所有选中的购物项
    @Getter @Setter
    List<OrderItemVo> items;

    //发票记录

    // 优惠信息
    @Getter @Setter
    Integer integration;

    @Getter @Setter
    Map<Long, Boolean> stocks;

    public Integer getCount() {
        int i = 0;
        if(items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

//    BigDecimal total;// 订单总额

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if(items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

//    BigDecimal payPrice; // 应付价格

    public BigDecimal getPayPrice() {
        return getTotal();
    }

    // 防止重复下单  防重令牌
    @Getter @Setter
    String orderToken;
}
