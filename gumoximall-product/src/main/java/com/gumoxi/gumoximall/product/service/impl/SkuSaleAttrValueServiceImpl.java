package com.gumoxi.gumoximall.product.service.impl;

import com.gumoxi.gumoximall.product.dao.SkuSaleAttrValueDao;
import com.gumoxi.gumoximall.product.entity.SkuSaleAttrValueEntity;
import com.gumoxi.gumoximall.product.service.SkuSaleAttrValueService;
import com.gumoxi.gumoximall.product.vo.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        SkuSaleAttrValueDao dao = this.baseMapper;
        List<SkuItemSaleAttrVo> saleAttrVos = dao.getSaleAttrsBySpuId(spuId);
        return saleAttrVos;
    }

    @Override
    public List<String> getSkuSaleAttrValues(Long skuId) {
        // SELECT CONCAT(attr_name,"：",attr_value) FROM pms_sku_sale_attr_value WHERE sku_id=1
        SkuSaleAttrValueDao baseMapper = this.baseMapper;

        return baseMapper.getSkuSaleAttrValues(skuId);
    }

}