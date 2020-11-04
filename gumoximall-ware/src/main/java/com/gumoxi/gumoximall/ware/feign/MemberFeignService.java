package com.gumoxi.gumoximall.ware.feign;

import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("gumoximall-member")
public interface MemberFeignService {

        /**
         * 信息
         */
        @RequestMapping("/member/memberreceiveaddress/info/{id}")
        public R addrInfo(@PathVariable("id") Long id);
}
