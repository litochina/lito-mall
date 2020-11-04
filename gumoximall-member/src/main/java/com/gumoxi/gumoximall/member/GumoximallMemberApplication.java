package com.gumoxi.gumoximall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients( basePackages = {"com.gumoxi.gumoximall.member.feign"})
@EnableDiscoveryClient
@SpringBootApplication
public class GumoximallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GumoximallMemberApplication.class, args);
    }

}
