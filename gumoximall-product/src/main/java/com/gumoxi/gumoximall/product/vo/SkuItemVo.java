package com.gumoxi.gumoximall.product.vo;

import com.gumoxi.gumoximall.product.entity.SkuImagesEntity;
import com.gumoxi.gumoximall.product.entity.SkuInfoEntity;
import com.gumoxi.gumoximall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;
@Data
public class SkuItemVo {
    // 1、sku基本信息 pms_sku_info
    SkuInfoEntity info;

    boolean hasStock = true;

    // 2、sku图片的获取 pms_sku_images
    List<SkuImagesEntity> images;

    // 3、获取spu销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    // 4、获取spu的介绍
    SpuInfoDescEntity desc;
    // 5、获取spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    //6、秒杀商品的优惠信息
    SeckillSkuInfoVo seckillInfo;

}
