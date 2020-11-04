package com.gumoxi.gumoximall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.member.entity.MemberEntity;
import com.gumoxi.gumoximall.member.vo.MemberLoginVo;
import com.gumoxi.gumoximall.member.vo.MemberRegistVo;
import com.gumoxi.gumoximall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 12:52:24
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone);

    void checkUsernameUnique(String username);

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

