package com.gumoxi.gumoximall.member.interceptor;

import com.gumoxi.gumoximall.common.constant.AuthServerConstant;
import com.gumoxi.gumoximall.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", uri);
        if(match) {
            return true;
        }

        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute != null) {
            threadLocal.set(attribute);
            return true;
        } else {
            // 没登录
            request.getSession().setAttribute("msg", "请登录");
            response.sendRedirect("http://auth.gumoxi.com/login.html");
            return false;
        }

    }
}
