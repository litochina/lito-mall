package com.gumoxi.gumoximall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumoxi.gumoximall.common.utils.PageUtils;
import com.gumoxi.gumoximall.common.utils.Query;
import com.gumoxi.gumoximall.product.dao.CategoryDao;
import com.gumoxi.gumoximall.product.entity.CategoryEntity;
import com.gumoxi.gumoximall.product.service.CategoryBrandRelationService;
import com.gumoxi.gumoximall.product.service.CategoryService;
import com.gumoxi.gumoximall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listByTree() {
        // 1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2、组装成父子的树形结构
        // 2.1、找到所有的一级分类
        // 2.2、为每一个分类查找子类

        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1.检查当前删除的菜单，是否在别的地方引用

        // 使用逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catalogId) {
        List<Long> paths = new ArrayList<Long>();
        List<Long> parentPaths = findParentPath(catalogId, paths);
        Collections.reverse(parentPaths);
        return parentPaths.toArray(new Long[]{});
    }

//    @CacheEvict(value = "category", key = "'getLevel1Categorys'")
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 1、每一个需要缓存的数据我们都来指定要放到哪个名字的缓存。【缓存分区（按照业务类型分）】
     * 2、@Cacheable({"category"})
     * 	代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     * 	如果缓存中没有，会调用方法，最后将方法的结果放入缓存。
     * 3、默认行为
     * 	1）、如果方法中有，方法不用调用，
     * 	2）、key默认自动生成，缓存的名字：：SimpleKey[](自动生成的Key)   category::SimpleKey []
     * 	3）、缓存的value值，默认使用jdk序列化机制，将序列化的序列存到redis
     * 	4）、默认ttl时间 -1
     *
     *    自定义：
     * 	1）、指定生成的缓存使用的key：key 属性指定，接受一个spEl
     * 	2）、指定缓存的额数据的存活时间，配置文件中修改ttl
     * 	3）、将数据保存为json格式
     * @return
     */
    @Cacheable(value = "category", key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("获取商品三级分类");
        List<CategoryEntity> categoryEntities = list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    //@Cacheable(value = "category", key = "#root.method.name")
    @Override
    public Map<Long, List<Catelog2Vo>> getCatalogJson() {
        /**
         * 1、将数据库的查询改成一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1 查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        // 2 封装数据
        Map<Long, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> {
            return k.getCatId();
        }, v -> {
            List<CategoryEntity> entities = getParent_cid(selectList, v.getCatId());

            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Categorys = getParent_cid(selectList, l2.getCatId());

                    if (level3Categorys != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = level3Categorys.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

//    @Override
    public Map<Long, List<Catelog2Vo>> getCatalogJson2() {

        /**
         * 1、空结果缓存：解决缓存穿透
         * 2、设置过期时间（加随机值）：解决缓存雪崩
         * 3、加锁：解决缓存击穿
         */

        // 1 从缓存中检索数据
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {

            // 2 缓存中没有数据，查询数据库，注意保证方法的原子性操作
            Map<Long, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();

            return catalogJsonFromDB;
        }
        System.out.println("缓存命中。。。");
        Map<Long, List<Catelog2Vo>> catelog2Vos = JSON.parseObject(catalogJSON, new TypeReference<Map<Long, List<Catelog2Vo>>>() {
        });
        return catelog2Vos;
    }


    /**
     * 缓存中的数据如何数据库中的数据保持一致
     * 缓存数据一致性 需要使用分布式锁
     * 1、双写模式
     * 2、失效模式
     *
     * @return
     */
    public Map<Long, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();
        System.out.println("获取锁成功，执行业务");
        Map<Long, List<Catelog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }
        return dataFromDB;
    }

    // 再次基于Redis进行缓存优化 本地锁锁不住分布式程序
    public Map<Long, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        // 1、占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        // 2. 加锁设锁过期失效必须是原子性的
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取锁成功，执行业务");
            Map<Long, List<Catelog2Vo>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();
            } finally {
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 3.删除锁
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDB;
        } else {
            System.out.println("获取锁失败，等待。。。");
            return getCatalogJsonFromDBWithRedisLock();
        }
    }

    private Map<Long, List<Catelog2Vo>> getDataFromDB() {
        // 获得锁以后要到缓存中在检查一次，看是否有数据，若果没有才继续查询
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<Long, List<Catelog2Vo>> catelog2Vos = JSON.parseObject(catalogJSON, new TypeReference<Map<Long, List<Catelog2Vo>>>() {
            });
            return catelog2Vos;
        }
        System.out.println("缓存不命中。。。查询数据库。。。");
        /**
         * 1、将数据库的查询改成一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1 查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        // 2 封装数据
        Map<Long, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> {
            return k.getCatId();
        }, v -> {
            List<CategoryEntity> entities = getParent_cid(selectList, v.getCatId());

            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Categorys = getParent_cid(selectList, l2.getCatId());

                    if (level3Categorys != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = level3Categorys.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));


        // 类型转化
        String s = JSON.toJSONString(parent_cid);
        // 3 查询到的数据缓存到Redis中
        redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);

        return parent_cid;
    }

    // 再次基于Redis进行缓存优化 本地锁锁不住分布式程序
    public Map<Long, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {


        // 只要是同一把锁，就能锁住需要这个锁的所有线程
        // 1 synchronized(this):SpringBoot 所有组件在容器中是单例的
        // TODO 本地锁：synchronized,JUC(lock)，在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            // 获得锁以后要到缓存中在检查一次，看是否有数据，若果没有才继续查询
            return getDataFromDB();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryEntities, Long catId) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(item -> item.getParentCid() == catId).collect(Collectors.toList());
        //return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    private List<Long> findParentPath(Long catalogId, List<Long> paths) {

        CategoryEntity last = getById(catalogId);
        paths.add(last.getCatId());
        if (last.getParentCid() != 0) {
            findParentPath(last.getParentCid(), paths);
        }
        return paths;
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((categoryEntity -> {
            // 找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        })).sorted((menu1, menu2) -> {
            // 子菜单排序

            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}