package com.gumoxi.gumoximall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(){
        // 创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.0.101:6379");

        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
