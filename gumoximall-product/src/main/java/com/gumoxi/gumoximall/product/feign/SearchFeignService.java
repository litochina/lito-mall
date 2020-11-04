package com.gumoxi.gumoximall.product.feign;

import com.gumoxi.gumoximall.common.to.es.SkuEsModel;
import com.gumoxi.gumoximall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gumoximall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList);
}
