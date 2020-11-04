package com.gumoxi.gumoximall.auth.feign;

import com.gumoxi.gumoximall.auth.vo.SocialUser;
import com.gumoxi.gumoximall.auth.vo.UserLoginVo;
import com.gumoxi.gumoximall.auth.vo.UserRegistVo;
import com.gumoxi.gumoximall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gumoximall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;
}
