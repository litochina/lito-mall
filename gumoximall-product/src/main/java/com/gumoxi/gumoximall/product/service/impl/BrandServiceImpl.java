package com.gumoxi.gumoximall.product.service.impl;

import com.gumoxi.gumoximall.product.dao.BrandDao;
import com.gumoxi.gumoximall.product.entity.BrandEntity;
import com.gumoxi.gumoximall.product.service.BrandService;
import com.gumoxi.gumoximall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        // 获取key
        String key  = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<BrandEntity>();
        if(!StringUtils.isEmpty(key)) {
            queryWrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetial(BrandEntity brand) {
        updateById(brand);

        if(!StringUtils.isEmpty(brand.getName())) {
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
        }

        // TODO 其他冗余更新
    }

    @Cacheable(value = "brands", key = "'brandsByIds'+#root.args[0]")
    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {
        List<BrandEntity> brandEntities = listByIds(brandIds);
        return brandEntities;
    }

}