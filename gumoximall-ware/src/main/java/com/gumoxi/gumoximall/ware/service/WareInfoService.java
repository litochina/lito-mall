package com.gumoxi.gumoximall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.ware.entity.WareInfoEntity;
import com.gumoxi.gumoximall.ware.vo.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-20 11:01:44
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 根据用户的收货地址计算运费
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}

