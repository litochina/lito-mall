package com.gumoxi.gumoximall.ware.feign;

import com.gumoxi.gumoximall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gumoximall-order")
public interface OrderFeignService {

    @RequestMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
