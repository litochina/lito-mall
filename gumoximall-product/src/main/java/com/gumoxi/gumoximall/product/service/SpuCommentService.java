package com.gumoxi.gumoximall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 08:47:28
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

