package com.gumoxi.gumoximall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.exception.NoStockException;
import com.gumoxi.gumoximall.common.to.SkuHasStockTo;
import com.gumoxi.gumoximall.common.to.mq.OrderTo;
import com.gumoxi.gumoximall.common.to.mq.StockDetailTo;
import com.gumoxi.gumoximall.common.to.mq.StockLockedTo;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;
import com.gumoxi.gumoximall.common.utils.R;
import com.gumoxi.gumoximall.ware.dao.WareSkuDao;
import com.gumoxi.gumoximall.ware.entity.WareOrderTaskDetailEntity;
import com.gumoxi.gumoximall.ware.entity.WareOrderTaskEntity;
import com.gumoxi.gumoximall.ware.entity.WareSkuEntity;
import com.gumoxi.gumoximall.ware.feign.OrderFeignService;
import com.gumoxi.gumoximall.ware.feign.ProcuctFeiginService;
import com.gumoxi.gumoximall.ware.service.WareOrderTaskDetailService;
import com.gumoxi.gumoximall.ware.service.WareOrderTaskService;
import com.gumoxi.gumoximall.ware.service.WareSkuService;
import com.gumoxi.gumoximall.ware.vo.OrderItemVo;
import com.gumoxi.gumoximall.ware.vo.OrderVo;
import com.gumoxi.gumoximall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProcuctFeiginService procuctFeiginService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 1、库存自动解锁。
     * 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁。
     * 2、订单失败
     * 解锁库存
     * <p>
     * <p>
     * <p>
     * 只要解锁库存的消息失败，一定要高数服务解锁失败
     *
     */


    private void unlockStock(Long skuId, Long wareId, Integer skuNum, Long detailId) {
        wareSkuDao.unlockStock(skuId, wareId, skuNum);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(detailId);
        wareOrderTaskDetailEntity.setLockStatus(2);// 变为已解锁
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }


    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 1、如果还没有这个库存则是新增操作
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));

        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setWareId(wareId);
            skuEntity.setStock(skuNum);
            skuEntity.setStockLocked(0);
            // TODO 远程查询sku的名字

            // 1、自己catch异常
            // 2、还有什么方法可以让异常出现后不会滚呢？高级
            try {
                R info = procuctFeiginService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }

            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockTo vo = new SkuHasStockTo();
            // SELECT SUM(stock-stock_locked) FROM `wms_ware_sku` WHERE sku_id = 1
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 默认只要试运行时异常都会回滚
     * (rollbackFor = NoStockException.class)
     *
     * @param vo
     * @return 库存解锁的场景
     * 1）、下单成功，订单过期没有支付被系统自动取消、被用户手动取消，都要解锁库存
     * 2）、下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     * 之前锁定的库存需要自动解锁。   （seata 的 at 分布式事务慢，所以用最终一致性）
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         * 保存库存工作单的详情
         * 追溯
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        //1、按照下单的收货地址，找一个就近仓库，锁库存
        // 1、找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            // 查询这个商品在哪个有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            stock.setNum(item.getCount());
            return stock;
        }).collect(Collectors.toList());

        Boolean allLock = true;
        // 2、锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            //1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
            //2、锁定失败，前面保存的工作单信息就会回滚了，发送出去的消息，即使要解锁记录，由于去数据库查不到id,所以就不用解锁
            // 2是不合理的  1:1-2-1 2:2-1-3 3:3-1-1(x)
            for (Long wareId : wareIds) {
                // UPDATE wms_ware_sku SET stock_locked = stock_locked + 5 WHERE sku_id = 2 AND ware_id = 2 AND stock - stock_locked > 5
                // 成功返回1 否则返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    // TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(),
                            taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(entity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());

                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    // 只发id不行，防止之前的回滚
                    stockLockedTo.setDetail(stockDetailTo);

                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                } else {
                    // 当前仓库锁失败，重试下一个仓库
                }
            }
            if (skuStocked == false) {
                // 当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }
        // 3、肯定全部锁定成功
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {

        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        // 解锁
        // 1、查询数据库关于这个订单锁定库存信息
        // 有：证明库存锁定成功了，
        //     解锁：
        //          1、没有这个订单，必须解锁
        //          2、有这个订单，不是解锁库存
        //              订单状态：已取消：解锁库存
        //                       没取消：不能解锁
        // 没有：库存锁定失败了，库存回滚了，这种情况不许解锁
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            //解锁
            Long taskId = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskId);
            String orderSn = taskEntity.getOrderSn();// 根据订单号检查订单状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                // 订单数据放回成功
                // UPDATE wms_ware_sku SET stock_locked=stock_locked-1 WHERE sku_id = 1 AND ware_id = 2
                OrderVo order = r.getData(new TypeReference<OrderVo>() {
                });
                if (order == null || order.getStatus() == 4) {
                    // 订单不存在了
                    //订单已被取消了，才能解锁库存
                    if(byId.getLockStatus() == 1) {
                        // 当前库存工作单详情，状态1 已锁定但是未解锁才可以解锁
                        unlockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                } else {
                    //消息拒绝以后重新放到消息队列中，让别人来继续消费解锁
                    throw new RuntimeException("远程服务失败");
                }
            }
        } else {
            // 无需解锁
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期。检查订单状态新建状态，什么都不做就走了。
     * 导致卡顿的订单永远不能解锁库存。
     * @param to
     */
    @Override

    public void unlockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        // 查一下最新库存的状态，防止重复解锁库存
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        //按照工作单找到所有 没有解锁的库存， 进行解锁
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id)
                .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            //Long skuId, Long wareId, Integer skuNum, Long detailId
            unlockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }
}