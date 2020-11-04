package com.gumoxi.gumoximall.product.web;

import com.gumoxi.gumoximall.product.service.SkuInfoService;
import com.gumoxi.gumoximall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 展示当前sku详情
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        System.out.println("parpring search " + skuId);
        SkuItemVo vo = skuInfoService.item(skuId);
        model.addAttribute("item", vo);
        return "item";
    }
}