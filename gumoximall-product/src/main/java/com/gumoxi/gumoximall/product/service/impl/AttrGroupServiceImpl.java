package com.gumoxi.gumoximall.product.service.impl;

import com.gumoxi.gumoximall.product.dao.AttrGroupDao;
import com.gumoxi.gumoximall.product.entity.AttrEntity;
import com.gumoxi.gumoximall.product.entity.AttrGroupEntity;
import com.gumoxi.gumoximall.product.service.AttrGroupService;
import com.gumoxi.gumoximall.product.service.AttrService;
import com.gumoxi.gumoximall.product.vo.AttrGroupWithAttrsVo;
import com.gumoxi.gumoximall.product.vo.SkuItemVo;
import com.gumoxi.gumoximall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isEmpty(key)) {
            wrapper.and((op)->{
                op.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        if(catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params), wrapper
            );

            return new PageUtils(page);
        } else {
            // select * from pms_attr_group where catelog_id = ? and (attr_group_id=? or attr_group_name like ?)
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> entities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        List<AttrGroupWithAttrsVo> vos = entities.stream().map(attrGroup -> {
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(attrGroup, attrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroup.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;
        }).collect(Collectors.toList());
        return vos;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 1、查询出当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
        // 1)、
        AttrGroupDao baseMapper = this.baseMapper;
        List<SpuItemAttrGroupVo> vo = baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        return vo;
    }

}