package com.gumoxi.gumoximall.order.listener;

import com.gumoxi.gumoximall.common.to.mq.SeckillOrderTo;
import com.gumoxi.gumoximall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo entity, Channel channel, Message message) throws IOException {
        System.out.println("准备创建秒杀单的详情：" + entity.getOrderSn());

        try {
            orderService.createSeckillOrder(entity);
            //手动调用支付宝收单；
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e){
            log.error(this.getClass().getName() + " 创建秒杀单异常: {}", e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
