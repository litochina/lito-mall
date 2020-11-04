package com.gumoxi.gumoximall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1、引入amqp场景：RabbitAutoConfiguration 就会自动生效
 * 2、给容器自动配置了
 *  RabbitTemplate AmqpAdmin CachingConnectionFactory RabbitMessagingTemplate
 * 3、给配置文件配置spring.rabbitmq信息
 * 4、@EnableRabit 开启功能
 * 5、监听消息使用@RabbitListener 必须有@EnableRabbit
 * <code>@RabbitListener</code>: 类+方法(监听了哪些队列即可)
 * <code>@RabbitHandler</code>: 标在方法上（重载区分不同的消息）
 *
 * 本地事务失效问题
 * 同一个对象内事务方法互调默认失效，原因，绕过了代理对象，事务使用代理对象来控制的
 * 解决：使用代理对象来调用事务方法
 * 	1）引入aop-start:spring-boot-starter-aop;引入aspectj
 * 	2）@EnableAspectJAutoProxy(exposeProxy = true):开启aspectj动态代理功能，以后所有的动态代理都是aspectj创建的
 * 	（即使没有接口也可以创建动态代理）,对外暴露代理对象
 *
 * 	OrderServiceImpl orderService = (OrderServiceImpl)AopContext.currentProxy();
 * 	orderService.b();
 * 	orderService.c();
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
public class GumoximallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GumoximallOrderApplication.class, args);
    }

}
