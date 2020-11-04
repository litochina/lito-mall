package com.gumoxi.gumoximall.search.controller;

import com.gumoxi.gumoximall.common.exception.BizCodeEnume;
import com.gumoxi.gumoximall.common.to.es.SkuEsModel;
import com.gumoxi.gumoximall.common.utils.Constant;
import com.gumoxi.gumoximall.common.utils.R;
import com.gumoxi.gumoximall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSearchSaveController {

    @Autowired
    ProductSaveService productSaveService;


    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsModelList);
        } catch (Exception e) {
            log.error("ElasticSearchSaveController商品上架错误：{}", e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(!b) {
            return R.ok();
        } else {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }

}
