package com.gumoxi.gumoximall.seckill.feign;

import com.gumoxi.gumoximall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gumoximall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/lastest3DaySession")
    R getLasted3DaySession();
}
