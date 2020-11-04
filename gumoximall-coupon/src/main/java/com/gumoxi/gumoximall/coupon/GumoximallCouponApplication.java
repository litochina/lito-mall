package com.gumoxi.gumoximall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.gumoxi.gumoximall.coupon.dao")
public class GumoximallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GumoximallCouponApplication.class, args);
    }

}
