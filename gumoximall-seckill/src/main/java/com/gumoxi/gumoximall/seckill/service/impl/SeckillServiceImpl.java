package com.gumoxi.gumoximall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.gumoxi.gumoximall.common.to.mq.SeckillOrderTo;
import com.gumoxi.gumoximall.common.utils.R;
import com.gumoxi.gumoximall.common.vo.MemberRespVo;
import com.gumoxi.gumoximall.seckill.feign.CouponFeignService;
import com.gumoxi.gumoximall.seckill.feign.ProductFeignService;
import com.gumoxi.gumoximall.seckill.interceptor.LoginUserInterceptor;
import com.gumoxi.gumoximall.seckill.service.SeckillService;
import com.gumoxi.gumoximall.seckill.to.SeckillSkuRedisTo;
import com.gumoxi.gumoximall.seckill.vo.SeckillSessionsWithSkus;
import com.gumoxi.gumoximall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // + 商品随机码
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1、烧苗需要秒杀的活动
        R r = couponFeignService.getLasted3DaySession();
        if (r.getCode() == 0) {
            List<SeckillSessionsWithSkus> data = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到redis
            //1、缓存活动信息
            saveSessionInfos(data);
            //2、缓存活动的关联商品信息
            saveSessionSkuInfos(data);
        }
    }

    // 返回当前时间可以参与的秒杀商品
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1、确定当前时间属于哪个秒杀场次
        // 1970 -
        long time = new Date().getTime();

        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSION_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                //2、获取这个秒杀场次所需要的的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list != null && list.size() > 0) {
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                        SeckillSkuRedisTo redis = JSON.parseObject(item, SeckillSkuRedisTo.class);
                        // redis.setRandomCode(null) ; 当前秒杀已经开始了
                        return redis;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }

        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        // 找到所有需要秒杀的商品额key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    //随机码
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    long current = new Date().getTime();
                    if (current >= startTime && current <= endTime) {
                    } else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }

            }
        }
        return null;
    }

    // 7_1
    // TODO 上架秒杀商品的时候，每一个数据都有过期时间
    // TODO 秒杀的后续流程，简化了收件人地址等信息
    @Override
    public String kill(String killId, String key, Integer num) {
        long s1 = System.currentTimeMillis();
        MemberRespVo respVo = LoginUserInterceptor.threadLocal.get();
        //1、获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = hashOps.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        } else {
            SeckillSkuRedisTo redisTo = JSON.parseObject(s, new TypeReference<SeckillSkuRedisTo>() {
            });
            // 校验合法想
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long time = new Date().getTime();
            long ttl = endTime - time;
            // 1、校验时间合法性
            if (time >= startTime && time <= endTime) {
                // 2、校验随机码和商品id
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                if (randomCode.equals(key) && skuId.equals(killId)) {
                    // 3、验证购物的数量是否合理
                    if (num <= redisTo.getSeckillLimit()) {
                        // 4、验证这个人是否已经购买过了。幂等性原则；如果只要秒杀成功就去站位。userId_SessionId_skuId
                        // SETNX
                        String redisKey = respVo.getId() + "_" + skuId;
                        //设置超时时间
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            // 站位成功说明从来没有买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            // 10 + 100
                            boolean b = semaphore.tryAcquire(num);
                            if (b) {
                                // 秒杀成功
                                // 快速下单 发送MQ 10  10+100+10=120ms 实际可能的耗时20ms
                                // 20ms controller到响应  来回 30ms  20+30=50ms  总共50ms 单线程 1s 的并发是 20 个  tomcat 500 个线程的话就是  10000 并发
                                // 如果之前的处理时间是 3s 那么 500 线程的话 并发量也就 200
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(respVo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                orderTo.setSkuId(redisTo.getSkuId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                long s2 = System.currentTimeMillis();
                                log.info("耗时...{}",(s2-s1));
                                return timeId;
                            }
                            return null;
                        } else {
                            // 已经购买过了
                            return null;
                        }

                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> data) {
        data.stream().forEach(session -> {
            //准备hash操作
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                if (!ops.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString())) {
                    //缓存商品
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    //1、sku的基本数据
                    R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillSkuRedisTo.setSkuInfo(skuInfo);
                    }
                    //2、sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo, seckillSkuRedisTo);

                    //3、设置当前商品的秒杀时间
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());
                    seckillSkuRedisTo.setSeckillLimit(seckillSkuVo.getSeckillLimit());

                    //4、随机吗
                    String token = UUID.randomUUID().toString().replace("-", "");
                    seckillSkuRedisTo.setRandomCode(token);

                    // 如果当前这个场次的商品的库存信息已经上架就不需要上架
                    //5、使用库存作为分布式信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());

                    String jsonString = JSON.toJSONString(seckillSkuRedisTo);
                    ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(), jsonString);
                }
            });
        });
    }


    private void saveSessionInfos(List<SeckillSessionsWithSkus> data) {
        data.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                if (collect != null && collect.size() > 0) {
                    // 缓存活动信息
                    redisTemplate.opsForList().leftPushAll(key, collect);
                }
            }
        });
    }
}
