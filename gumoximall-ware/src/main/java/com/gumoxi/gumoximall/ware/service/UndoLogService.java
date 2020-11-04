package com.gumoxi.gumoximall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-20 11:01:44
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

