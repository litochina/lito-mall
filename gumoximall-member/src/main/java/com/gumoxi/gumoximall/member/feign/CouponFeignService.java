package com.gumoxi.gumoximall.member.feign;

import com.gumoxi.gumoximall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gumoximall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/coupons")
    R memberCoupons();

}
