package com.gumoxi.gumoximall.order;

import com.gumoxi.gumoximall.order.entity.OrderEntity;
import com.gumoxi.gumoximall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest
class GumoximallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 测试发送消息
     */
    @Test
    void sendMessageTest() {
        // 1、发送消息，如果我们发送的消息是对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable

        // 2、发送的对象类型的消息，可以转成一个json
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setName("mm" + i);
                reasonEntity.setCreateTime(new Date());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
                log.info("消息发送完成{}", reasonEntity);
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setBillHeader("for me" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity);
                log.info("消息发送完成{}", orderEntity);
            }
        }
    }

    /**
     * 1、如何创建Exchange Queue Binding
     * 1)使用AmqpAdmin
     * 2、如何收发消息
     */
    @Test
    void contextLoads() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("创建[{}]交换机", "hello-java-exchange");
    }

    @Test
    void createQueue() {
        // this.name, this.durable, this.exclusive, this.autoDelete, new HashMap(this.getArguments())
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("创建[{}]队列", "hello-java-queue");
    }

    @Test
    void createBinding() {
        // String destination, Binding.DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("创建[{}]绑定", "hello-java-binding");
    }


}
