package com.gumoxi.gumoximall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GumoximallCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {

        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();

        // 配置跨域信息
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedHeader("*");
        config.addAllowedOrigin("*"); // Access-Control-Allow-Origin：支持哪些来源的请求跨域
        config.addAllowedMethod("*"); // Access-Control-Allow-Methods：支持哪些方法跨域
        config.setAllowCredentials(true); // Access-Control-Allow-Credentials：跨域请求默认不包含cookie，设置为true可以包含 cookie
        /**
         * CORS请求时，XMLHttpRequest对象的getResponseHeader()方法只能拿到6个基本字段：
         * Cache-Control、Content-Language、Content-Type、Expires、Last-Modified、Pragma。如
         * 果想拿到其他字段，就必须在Access-Control-Expose-Headers里面指定。
         */
        // Access-Control-Expose-Headers：跨域请求暴露的字段

        /**
         * Access-Control-Max-Age：表明该响应的有效时间为多少秒。在有效时间内，浏览器无
         * 须为同一请求再次发起预检请求。请注意，浏览器自身维护了一个最大有效时间，如果
         * 该首部字段的值超过了最大有效时间，将不会生效。
         */

        corsConfigurationSource.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(corsConfigurationSource);
    }
}
