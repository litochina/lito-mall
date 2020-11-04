package com.gumoxi.gumoximall.seckill;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合Sentinel
 *  1)、导入依赖 spring-cloud-starter-alibaba-sentinel
 *  2)、下载sentinel控制台
 *  3）、配置sentinel控制台地址信息
 *  4)、在控制台调整参数【默认所有的留空设置保存在内存中，重启失效】
 *
 *
 * 2、每个微服务导入actuator
 *      management.endpoints.web.exposure.include=*
 * 3、自定义流控 返回
 */
@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GumoximallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GumoximallSeckillApplication.class, args);
    }

}
