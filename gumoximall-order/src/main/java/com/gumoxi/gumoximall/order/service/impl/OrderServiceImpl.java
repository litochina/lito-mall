package com.gumoxi.gumoximall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.gumoxi.gumoximall.common.exception.NoStockException;
import com.gumoxi.gumoximall.common.to.SkuHasStockTo;
import com.gumoxi.gumoximall.common.to.mq.OrderTo;
import com.gumoxi.gumoximall.common.to.mq.SeckillOrderTo;
import com.gumoxi.gumoximall.common.utils.R;
import com.gumoxi.gumoximall.common.vo.MemberRespVo;
import com.gumoxi.gumoximall.order.constant.OrderConstant;
import com.gumoxi.gumoximall.order.constant.OrderStatusEnum;
import com.gumoxi.gumoximall.order.entity.OrderItemEntity;
import com.gumoxi.gumoximall.order.entity.PaymentInfoEntity;
import com.gumoxi.gumoximall.order.feign.CartFeignService;
import com.gumoxi.gumoximall.order.feign.MemberFeignService;
import com.gumoxi.gumoximall.order.feign.ProductFeignService;
import com.gumoxi.gumoximall.order.feign.WmsFeignService;
import com.gumoxi.gumoximall.order.interceptor.LoginUserInterceptor;
import com.gumoxi.gumoximall.order.service.OrderItemService;
import com.gumoxi.gumoximall.order.service.PaymentInfoService;
import com.gumoxi.gumoximall.order.to.OrderCreateTo;
import com.gumoxi.gumoximall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;

import com.gumoxi.gumoximall.order.dao.OrderDao;
import com.gumoxi.gumoximall.order.entity.OrderEntity;
import com.gumoxi.gumoximall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderService orderService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Transactional
    public void a() {
//        b();
//        c();
        OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
        orderService.b();
        orderService.c();
    }

    @Transactional
    public void b() {

    }

    @Transactional
    public void c() {

    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        //获取之前的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //1、远程查询所有收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            orderConfirmVo.setAddress(address);
        }, executor);
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2、远程查询购物车中选中的购物项信息
            List<OrderItemVo> orderItemVos = cartFeignService.currentMemberCartItems();
            orderConfirmVo.setItems(orderItemVos);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R skuHasStock = wmsFeignService.getSkuHasStock(collect);
            List<SkuHasStockTo> data = skuHasStock.getData(new TypeReference<List<SkuHasStockTo>>() {
            });
            if(data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                orderConfirmVo.setStocks(map);
            }
        }, executor);

        //3、查询积分
        Integer integration = memberRespVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        //5 TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        //4、其他数据自动计算
        CompletableFuture.allOf(addressFuture, cartFuture).get();

        return orderConfirmVo;
    }

//    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        orderSubmitVoThreadLocal.set(vo);
        SubmitOrderResponseVo response  = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        response.setCode(0);

        //1、验证令牌
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
//        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId());
        // 原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if(result == 0L) {
            //令牌验证失败
            response.setCode(1);
            return  response;
        } else {
            // 令牌验证成功
            // 创建订单，验令牌，验价格，锁库存。。。
            OrderCreateTo order = createOrder();
            // 2、验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if(Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 金额对别
                // TODO 3、保存订单
                saveOrder(order);
                // 4、库存锁定，只要有异常，回滚订单数据
                // 订单号，所有订单项（skuId,skuName,num）
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());

                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                // TODO 4 远程锁库存   // 订单回滚，库存不会滚

                // 为了保证高并发。库存服务自己回滚，可以发消息给库存服务
                // 库存本身也可以使用自动解锁模式
                R r = wmsFeignService.orderLockStock(lockVo);
                if(r.getCode() == 0) {
                    //锁定成功
                    response.setOrder(order.getOrder());

                    // TODO 5 远程扣减积分
                   // int i = 10/0; // 订单回滚，库存不会滚
                    // TODO 订单创建成功，发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return response;
                } else{
                    //锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            } else {
                response.setCode(2);
                return response;
            }
        }
    }

    @Override
    public OrderEntity getOrderStatus(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前订单的最新状态

        OrderEntity orderEntity = this.getById(entity.getId());
        if(orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            // 关单
            OrderEntity order = new OrderEntity();
            order.setId(entity.getId());
            order.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(order);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            // 发送MQ
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }

    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        if(orderEntity != null) {
            BigDecimal scale = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
            payVo.setTotal_amount(scale.toString());
        }
        payVo.setOut_trade_no(orderSn);
        List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity itemEntity = orderItemEntities.get(0);
        payVo.setSubject(itemEntity.getSkuName());
        payVo.setBody(itemEntity.getSkuAttrsVals());

        return payVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> collect = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setOrderItemEntities(order_sn);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(collect);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝的支付结果
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //1、保存交易流水，用于账单对账
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());
        paymentInfoService.save(infoEntity);

        //2、修改订单状态
        if(vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            //支付成功状态
            String outTradeNo = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }
        return null;
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo entity) {
        // TODO 保存订单信息
        OrderEntity orderEntity  = new OrderEntity();
        orderEntity.setOrderSn(entity.getOrderSn());
        orderEntity.setMemberId(entity.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        BigDecimal multiply = entity.getSeckillPrice().multiply(new BigDecimal("" + entity.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        // TODO 保存订单项
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(entity.getOrderSn());
        itemEntity.setRealAmount(multiply);
        // TODO 获取当前SKU的详细信息进行设置 producFeiginService.getSpuInfoBySkuId();
        itemEntity.setSkuQuantity(entity.getNum());
        orderItemService.save(itemEntity);
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        for (int i = 0; i < orderItems.size(); i++) {
            orderItemService.save(orderItems.get(i));
        }
//        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        // 1、生产个一个订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrder(orderSn);

        // 2、订单项信息
        List<OrderItemEntity> orderItems =  buildOrderItems(orderSn);
        //3、计算价格相关
        computePrice(order, orderItems);

        createTo.setOrder(order);
        createTo.setOrderItems(orderItems);

        return createTo;
    }

    private void computePrice(OrderEntity order, List<OrderItemEntity> orderItems) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal coupon = new BigDecimal("0");
        BigDecimal integration = new BigDecimal("0");
        BigDecimal promotion = new BigDecimal("0");
        BigDecimal gift = new BigDecimal("0");
        BigDecimal growth = new BigDecimal("0");

        //订单的总额，叠加每一个订单项的总额
        for (OrderItemEntity orderItem : orderItems) {
            coupon = coupon.add(orderItem.getCouponAmount());
            integration = integration.add(orderItem.getIntegrationAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            total  = total.add(orderItem.getRealAmount());
            gift = gift.add(new BigDecimal(orderItem.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(orderItem.getGiftGrowth().toString()));
        }
        //1、订单价格相关
        order.setTotalAmount(total);
        // 应付总额
        order.setPayAmount(total.add(order.getFreightAmount()));

        order.setPromotionAmount(promotion);
        order.setCouponAmount(coupon);
        order.setIntegrationAmount(integration);

        // 设置积分信息
        order.setIntegration(gift.intValue());
        order.setGrowth(growth.intValue());
        order.setDeleteStatus(0);//未删除
    }

    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        //创建订单号
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberRespVo.getId());
        //获取收货信息
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        // 运费
        entity.setFreightAmount(fareResp.getFare());
        // 收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());
        // 订单状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);

        return entity;
    }

    /**
     * 构建所有订单项数据
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项的价格
        List<OrderItemVo> currentMemberCartItems = cartFeignService.currentMemberCartItems();
        if(currentMemberCartItems != null && currentMemberCartItems.size() > 0) {
            List<OrderItemEntity> collect = currentMemberCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    /**
     * 构建某一个订单项数据
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1、订单信息：订单号   v
        // 2、商品的SPU信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getCatalogId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());
        // 3、商品的SKU信息   v
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        itemEntity.setSkuQuantity(cartItem.getCount());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        // 4、优惠信息（不作）
        // 5、积分信息   v
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString())).intValue());
        // 6、订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额  总额-优惠
        BigDecimal orgin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orgin.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

}