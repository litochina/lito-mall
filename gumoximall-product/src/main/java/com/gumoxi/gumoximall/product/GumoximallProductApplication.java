package com.gumoxi.gumoximall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients("com.gumoxi.gumoximall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.gumoxi.gumoximall.product.dao")
@SpringBootApplication
public class GumoximallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GumoximallProductApplication.class, args);
    }

}
