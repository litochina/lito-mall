package com.gumoxi.gumoximall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.gumoxi.gumoximall.common.exception.BizCodeEnume;
import com.gumoxi.gumoximall.member.exception.PhoneExistException;
import com.gumoxi.gumoximall.member.exception.UsernameExistException;
import com.gumoxi.gumoximall.member.feign.CouponFeignService;
import com.gumoxi.gumoximall.member.vo.MemberLoginVo;
import com.gumoxi.gumoximall.member.vo.MemberRegistVo;
import com.gumoxi.gumoximall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.gumoxi.gumoximall.member.entity.MemberEntity;
import com.gumoxi.gumoximall.member.service.MemberService;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.R;



/**
 * 会员
 *
 * @author lishuangyang
 * @email 2522484379@qq.com
 * @date 2020-04-15 12:52:24
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;


    @PostMapping("/oauth2/login")
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception {
        MemberEntity member = memberService.login(socialUser);
        if(member != null) {
            return R.ok().setData(member);
        } else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTIION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTIION.getMsg());
        }

    }

    @RequestMapping("/coupons")
    public R coupons(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("momo");

        R memberCoupons = couponFeignService.memberCoupons();

        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }


    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity member = memberService.login(vo);
        if(member != null) {
            return R.ok().setData(member);
        } else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTIION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTIION.getMsg());
        }

    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){

        try {
            memberService.regist(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
