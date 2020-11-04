package com.gumoxi.gumoximall.product.web;

import com.gumoxi.gumoximall.product.entity.CategoryEntity;
import com.gumoxi.gumoximall.product.service.CategoryService;
import com.gumoxi.gumoximall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        // 查找所有的一级分类
        List<CategoryEntity> categorys = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categorys);
        return "index";
    }

    /**
     * 返回json 数据而不是跳转界面 所以 @ResponseBody
     * @return
     */
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<Long, List<Catelog2Vo>> getCatalogJson() {
        Map<Long, List<Catelog2Vo>> map  =categoryService.getCatalogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 1.获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");
        // 2.加锁 阻塞式等待，默认加锁都是30s时间
        // 1).锁的自动续期,如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被掉
        // 2).加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，默认在30s以后自动删除
//        lock.lock();

        lock.lock(30, TimeUnit.SECONDS);
        // 问题 lock.lock(10, TimeUnit.SECONDS); 在锁时间到了以后不会自动续期
        // 1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        // 2、如果我们没有指定锁的超时时间，就使用30*1000【LockWatchdaoTimeout看门狗的默认时间】
        //      只要占锁成功，就会启动一个定时任务【重新给锁设置时间，新的过期时间就是看门狗的默认时间】，每隔10s就会自动续期
        //      【LockWatchdaoTimeout看门狗的默认时间】/3时间
        // 最佳实战
        // 1.lock.lock(30, TimeUnit.SECONDS); 省掉了整个续期操作，
        try {
            System.out.println("获取分布式锁，执行业务操作。" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {

        } finally {
            System.out.println("释放锁。" + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }


    /**
     * 使用场景保证一定能读到最新数据，修改期间，写锁是一个排它锁（互斥锁/独享）。读锁是一个共享锁，写锁没有释放，读就必须等待
     *
     * 读+读：相当于无锁，并发读，只会在redis中记录好，所有当前的读写，他们都同时加锁成功
     * 写+读：等待写锁释放
     * 写+写：阻塞方式
     * 读+写：有读锁。写也需要等待
     * 只要有写的存在，都必须等待
     * @return
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        rLock.lock();
        String s = "";
        try {
            System.out.println("获取写锁，执行业务操作。" + Thread.currentThread().getId());
            Thread.sleep(30000);
            s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放写锁。" + Thread.currentThread().getId());
            rLock.unlock();
        }
        return s;
    }


    /**
     * 使用场景保证一定能读到最新数据
     * @return
     */
    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        rLock.lock();
        String s = "";
        try {
            System.out.println("获取读锁，执行业务操作。" + Thread.currentThread().getId());
            Thread.sleep(30000);
            s = redisTemplate.opsForValue().get("writeValue");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放读锁。" + Thread.currentThread().getId());
            rLock.unlock();
        }
        return s;
    }

    /**
     * 车库停车
     * 3个车位
     * 信号量也可以用做分布式限流
     * @return
     */
    @ResponseBody
    @GetMapping("/park")
    public String park() {
        RSemaphore park = redisson.getSemaphore("park");
        boolean b = park.tryAcquire();
        if(b) {
            // 执行业务
        } else {
            return "error";
        }
        return  "ok=>" + b;
    }

    @ResponseBody
    @GetMapping("/go")
    public String go() {
        RSemaphore park = redisson.getSemaphore("park");
        park.release(); // 释放一个车位

//        Semaphore semaphore = new Semaphore(5);
//        semaphore.release();
//
//        semaphore.acquire();

        return  "ok";
    }

    /**
     * 闭锁,信号量
     *
     * 放假，锁门
     * 1班没人了，2班没人了
     * 5个班级全部走完
     */
    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();
        return "放假了。。。";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String lockDoor(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();

        return id+"班的人都走了。。。";
    }

}
