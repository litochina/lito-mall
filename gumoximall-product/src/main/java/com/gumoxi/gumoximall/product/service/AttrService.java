package com.gumoxi.gumoximall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.product.entity.AttrEntity;
import com.gumoxi.gumoximall.product.vo.AttrGroupRelationVo;
import com.gumoxi.gumoximall.product.vo.AttrRespVo;
import com.gumoxi.gumoximall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 08:47:28
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getRelationNoAttr(Map<String, Object> params, Long attrgroupId);

    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

