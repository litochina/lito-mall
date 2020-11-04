package com.gumoxi.gumoximall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.gumoxi.gumoximall.common.utils.R;
import com.gumoxi.gumoximall.product.config.MyThreadConfig;
import com.gumoxi.gumoximall.product.dao.SkuInfoDao;
import com.gumoxi.gumoximall.product.entity.SkuImagesEntity;
import com.gumoxi.gumoximall.product.entity.SkuInfoEntity;
import com.gumoxi.gumoximall.product.entity.SpuInfoDescEntity;
import com.gumoxi.gumoximall.product.feign.SeckillFeignService;
import com.gumoxi.gumoximall.product.service.*;
import com.gumoxi.gumoximall.product.vo.SeckillSkuInfoVo;
import com.gumoxi.gumoximall.product.vo.SkuItemSaleAttrVo;
import com.gumoxi.gumoximall.product.vo.SkuItemVo;
import com.gumoxi.gumoximall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo vo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、sku基本信息 pms_sku_info
            SkuInfoEntity info = getById(skuId);
            vo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3、获取spu销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            vo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4、获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            vo.setDesc(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5、获取spu的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            vo.setGroupAttrs(attrGroupVos);
        }, executor);


        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2、sku图片的获取 pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            vo.setImages(images);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            // 查询当前sku是否参与秒杀优惠
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillSkuInfoVo data = r.getData(new TypeReference<SeckillSkuInfoVo>() {
                });
                vo.setSeckillInfo(data);
            }
        }, executor);

        // 等待所有任务都完成呢个
        CompletableFuture.allOf(saleAttrFuture,descFuture,baseAttrFuture,imageFuture,seckillFuture).get();

        return vo;
    }

}