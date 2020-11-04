package com.gumoxi.gumoximall.member.dao;

import com.gumoxi.gumoximall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 12:52:24
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();
}
