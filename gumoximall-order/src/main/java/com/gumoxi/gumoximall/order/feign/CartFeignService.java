package com.gumoxi.gumoximall.order.feign;

import com.gumoxi.gumoximall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("gumoximall-cart")
public interface CartFeignService {

    @GetMapping("/currentMemberCartItems")
    List<OrderItemVo> currentMemberCartItems();
}
