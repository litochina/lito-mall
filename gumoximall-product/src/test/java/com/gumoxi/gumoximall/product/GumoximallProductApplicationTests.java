package com.gumoxi.gumoximall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gumoxi.gumoximall.product.dao.AttrGroupDao;
import com.gumoxi.gumoximall.product.dao.SkuSaleAttrValueDao;
import com.gumoxi.gumoximall.product.entity.BrandEntity;
import com.gumoxi.gumoximall.product.service.BrandService;
import com.gumoxi.gumoximall.product.vo.SkuItemSaleAttrVo;
import com.gumoxi.gumoximall.product.vo.SkuItemVo;
import com.gumoxi.gumoximall.product.vo.SpuItemAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
class GumoximallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    void test() {
        List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueDao.getSaleAttrsBySpuId(2L);
        System.out.println(saleAttrVos);
    }

    @Test
    void testAttrGroup() {
        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupDao.getAttrGroupWithAttrsBySpuId(2L, 225L);
        System.out.println(attrGroupVos);
    }


    @Test
    void redisson() {

        System.out.println(redissonClient);
    }

    @Test
    void redis() {

        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        ops.set("hello", "world_"+ UUID.randomUUID().toString());

        System.out.println("取出数据："+ops.get("hello"));
    }


    @Test
    void contextLoads() {

//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("apple");
//        brandService.save(brandEntity);

        List<BrandEntity> list = brandService.list(new QueryWrapper<>());
        list.forEach((x)->{
            System.out.println(x);
        });
    }

}
