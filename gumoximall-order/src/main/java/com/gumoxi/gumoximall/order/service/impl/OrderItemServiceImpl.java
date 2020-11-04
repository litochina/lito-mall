package com.gumoxi.gumoximall.order.service.impl;

import com.gumoxi.gumoximall.order.entity.OrderEntity;
import com.gumoxi.gumoximall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;

import com.gumoxi.gumoximall.order.dao.OrderItemDao;
import com.gumoxi.gumoximall.order.entity.OrderItemEntity;
import com.gumoxi.gumoximall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 声明需要监听的所有队列
     * @param message
     */
    @RabbitListener(queues = {"hello-java-queue"})
    public void receiveMessage(Message message) {
        System.out.println("接受到消息...内容"+message+" 消息类" + message.getClass());
    }

    /**
     * 声明需要监听的所有队列
     *  org.springframework.amqp.core.Message
     *  参数可以写一下类型
     *  1、Message message :原生消息详细信息。头+体
     *  2、T<发送的消息类型> OrderReturnReasonEntity
     *  3、Channel channel:当前传输数据的通道
     *
     *  Queue:可以很多人都来监听。只要收到消息，队列数据删除消息，而且  只能有一个收到次此消息
     *  场景：
     *      1） 订单服务启动多个，同一个消息，只能有一个客户端接收
     *      2） 只有一个消息完全处理完，方法运行结束，我们就可以接受下一个消息
     * @param message
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessageNative(Message message, OrderReturnReasonEntity content, Channel channel) throws InterruptedException {
        System.out.println("接受到消息..."+content);
        byte[] body = message.getBody();
        //消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
//        Thread.sleep(3000);
        System.out.println("消息处理完成..."+content.getName());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag==>" + deliveryTag);

        // 签收货物，非批量模式
        try {
//            if(deliveryTag%2 == 0) {
                channel.basicAck(deliveryTag, false);
                System.out.println("签收货物..." + deliveryTag);
//            } else {
//                channel.basicNack(deliveryTag, false, true);
//                System.out.println("没有签收货物..." + deliveryTag);
//            }

        } catch (IOException e) {
            e.printStackTrace();
            // 网络中断
        }
    }

//    @RabbitHandler
    public void receiveMessageNative2(OrderEntity content) throws InterruptedException {
        System.out.println("接受到消息..."+content);
    }

}