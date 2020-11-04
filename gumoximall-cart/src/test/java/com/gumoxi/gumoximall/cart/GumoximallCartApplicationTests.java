package com.gumoxi.gumoximall.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class GumoximallCartApplicationTests {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
//        String cartKey = "cartKey";
//        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
//        operations.put("1", "{name:lishuangyang,wife:gumoxi}");
//
//        Object o = operations.get(cartKey);
//        System.out.println(o);
//        Object o1 = operations.get("1");
//        System.out.println(o1);
    }

}
