package com.gumoxi.gumoximall.order.controller;

import com.gumoxi.gumoximall.order.entity.OrderEntity;
import com.gumoxi.gumoximall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMQ")
    public String sendMQ() {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setName("mm" + i);
                reasonEntity.setCreateTime(new Date());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setBillHeader("for me" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity);
            }
        }
        return "ok";
    }
}
