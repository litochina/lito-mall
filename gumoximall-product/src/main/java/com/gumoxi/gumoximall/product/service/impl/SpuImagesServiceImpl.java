package com.gumoxi.gumoximall.product.service.impl;

import com.gumoxi.gumoximall.product.dao.SpuImagesDao;
import com.gumoxi.gumoximall.product.entity.SpuImagesEntity;
import com.gumoxi.gumoximall.product.service.SpuImagesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveImages(Long id, List<String> images) {
        if(images == null || images.size() == 0) { return ;}

        List<SpuImagesEntity> spuImagesEntities = images.stream().map(img -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setImgUrl(img);
            spuImagesEntity.setSpuId(id);
            return spuImagesEntity;
        }).collect(Collectors.toList());

        this.saveBatch(spuImagesEntities);
    }

}