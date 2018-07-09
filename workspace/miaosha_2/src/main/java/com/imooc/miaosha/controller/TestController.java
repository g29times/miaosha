package com.imooc.miaosha.controller;

import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.lock.RedisLock;
import com.imooc.miaosha.redis.lock.StockKey;
import com.imooc.miaosha.util.ConcurrentUtil;
import com.imooc.miaosha.util.id.SpecAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static com.imooc.miaosha.redis.RedisService.returnToPool;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    RedisService redisService;

    @Autowired
    JedisPool jedisPool;

    @GetMapping("/lock")
    @ResponseBody
    @SpecAnnotation(desc = "testlock")
    public Object webTest(@RequestParam String user) throws Exception {
        Jedis jedis = null;
        String serviceId = Thread.currentThread().getName();
        String lockKey = "USERLOCK";
        try {
            jedis = jedisPool.getResource();
            // 1.此处失效时间应大于业务操作时间
            if (RedisLock.tryLock(jedis, lockKey, serviceId, 30000)) {
                System.out.println(user + " | " + serviceId + "【GET LOCK】");
                // 2.执行和睡眠先后顺序很重要
                Thread.sleep(10);
                System.out.println(user + " | " + serviceId + "【FINISH JOB】");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        } finally {
            returnToPool(jedis);
            if (RedisLock.tryUnlock(jedis, lockKey, serviceId)) {
                System.out.println(user + " | " + serviceId + "【UNLOCK】");
            } else {
                return false;
            }
        }
    }

    @GetMapping("/lock/{threadNum}")
    @ResponseBody
    @SpecAnnotation(desc = "testlock")
    public Object localTest(@PathVariable Integer threadNum) throws Exception {

        // 初始化
//        redisService.set(StockKey.getByNum, "", stockNumber);

        // 单线程测试
//        Jedis jedis = jedisPool.getResource();
//        String serviceId = "service1";
//        try {
//            if (RedisLock.tryLock(jedis, "LOCK" + ":" + "STOCK", serviceId, 3000)) {
//                redisService.plus(StockKey.getByNum, "");
//
//                System.out.println(Thread.currentThread().getName());
//                Thread.sleep(3000);
//            }
//            stockNumber = redisService.get(StockKey.getByNum, "", Integer.class);
//        } finally {
//            RedisLock.tryUnlock(jedis, "LOCK" + ":" + "STOCK", serviceId);
//            returnToPool(jedis);
//        }

        // 多线程测试
        ConcurrentUtil.conTest(threadNum);

        Integer stockNumber = redisService.get(StockKey.getByNum, "", Integer.class);
        return "success modify stockNumber to " + stockNumber;
    }

    public static void main(String[] args) {
        ConcurrentUtil.conTest(500);
    }


}
