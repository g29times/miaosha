package com.imooc.miaosha.controller;

import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.lock.StockKey;
import com.imooc.miaosha.redis.lock.RedisLock;
import com.imooc.miaosha.service.id.IdUtil;
import com.imooc.miaosha.service.id.SpecAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController("/test")
public class TestIdController {

    @Autowired
    RedisService redisService;

    /**
     * 获取分布式ID
     *
     * @return
     */
    @GetMapping("/id")
    @SpecAnnotation(desc = "login")
    public Object getNextId() {
        IdUtil center0worker0 = IdUtil.getInstance(0, 0);
        return center0worker0.nextId();
    }

//    /**
//     * redis插入
//     *
//     * @return
//     */
//    @GetMapping("/redis/set")
//    @SpecAnnotation(desc = "redisSet")
//    public Object redisSet() {
//        OrderEntity order = new OrderEntity();
//        String orderId = "1";
//        order.setId(orderId);
//        order.setType(1);
//        redisService.set(OrderKey.getById, "" + orderId, order); // OrderKey:id1
//        return ResultData.success("cache order success", order);
//
//    }
//
//    /**
//     * redis获取
//     *
//     * @param orderId
//     * @return
//     */
//    @GetMapping("/redis/get")
//    @SpecAnnotation(desc = "redisGet")
//    public Object redisGet(String orderId) {
//        OrderEntity order = redisService.get(OrderKey.getById, "" + orderId, OrderEntity.class);
//        return order;
//    }

    /**
     * 分布并发修改库存数量
     *
     * @param
     * @return
     */
    @GetMapping("/lock/modify")
    @SpecAnnotation(desc = "lockModify")
    public Object lockModify(/*Integer stockNumber*/) throws Exception {

//        redisService.set(StockKey.getByNum, "", stockNumber);

        // 单线程测试
//        Jedis jedis = jedisPool.getResource();
//        String serviceId = "service1";
//        try {
//            if (RedisLock.tryLock(jedis, stockNumber.toString(), serviceId, 3000)) {
//                redisService.plus(StockKey.getByNum, "");
//
//                System.out.println(Thread.currentThread().getName());
//                Thread.sleep(3000);
//            }
//            stockNumber = redisService.get(StockKey.getByNum, "", Integer.class);
//        } finally {
//            RedisLock.tryUnlock(jedis, stockNumber.toString(), serviceId);
//            returnToPool(jedis);
//        }

        // 多线程测试
        Integer stockNumber = redisService.get(StockKey.getByNum, "", Integer.class);
        Integer threadNum = 3;
        ExecutorService pool = new ThreadPoolExecutor(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < threadNum; i++) {
            pool.execute(new Busi(stockNumber.toString()));
        }
        pool.shutdown();

        stockNumber = redisService.get(StockKey.getByNum, "", Integer.class);
        return "modify stockNumber success" + stockNumber;
    }

    public class Busi implements Runnable {

        private String stockNumber;

        public Busi() {
        }

        public Busi(String stockNumber) {
            this.stockNumber = stockNumber;
        }

        @Override
        public synchronized void run() {
            Jedis jedis = jedisPool.getResource();
            String serviceId = Thread.currentThread().getName();
            try {
                if (RedisLock.tryLock(jedis, "LOCK" + ":" + "STOCK", serviceId, 30000)) {
                    System.out.println(serviceId + " get lock");
                    redisService.plus(StockKey.getByNum, "");
                    Thread.sleep(10000);
                    System.out.println("------------------------");
                    System.out.println(serviceId + " finish work");
                } else {
                    System.out.println(serviceId + " doesn't get lock");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (RedisLock.tryUnlock(jedis, "LOCK" + ":" + "STOCK", serviceId)) {
                    System.out.println(serviceId + " unlock");
                } else {
                    System.out.println(serviceId + " doesn't have lock");
                }
                returnToPool(jedis);
            }
        }
    }

    @Autowired
    private JedisPool jedisPool;

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

}
