package com.gumoxi.gumoximall.product.feign;

import com.gumoxi.gumoximall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gumoximall-ware")
public interface WareFeignService {

    /**
     * 1、R 设计的时候可以加上泛型  返回的数据类型Spring MVC 会自动封装
     * 2、直接放回List类型结果
     * 3、自己解析
     *
     * @param skuIds
     * @return
     */
    @PostMapping(value = "/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}
