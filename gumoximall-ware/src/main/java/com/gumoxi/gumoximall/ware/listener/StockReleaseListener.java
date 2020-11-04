package com.gumoxi.gumoximall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.gumoxi.gumoximall.common.to.mq.OrderTo;
import com.gumoxi.gumoximall.common.to.mq.StockDetailTo;
import com.gumoxi.gumoximall.common.to.mq.StockLockedTo;
import com.gumoxi.gumoximall.common.utils.R;
import com.gumoxi.gumoximall.ware.entity.WareOrderTaskDetailEntity;
import com.gumoxi.gumoximall.ware.entity.WareOrderTaskEntity;
import com.gumoxi.gumoximall.ware.service.WareSkuService;
import com.gumoxi.gumoximall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handlerStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("定期自动解锁库存...");

        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e){
            System.out.println("异常信息..." + e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handlerOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭准备解锁库存...");

        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e){
            System.out.println("异常信息..." + e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
