package com.imooc.miaosha.controller;

import com.imooc.miaosha.redis.RedisLock;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.util.SpecAnnotation;
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

    private static int stockNum = 5;

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
//                stockNum++;
                Thread.sleep(50);
                if(stockNum <= 0) {
                    System.out.println(user + " | " + serviceId + "业务退出");
                    return false;
                } else { // 执行
                    stockNum--;
                    Thread.sleep(50);
                }

                System.out.println(user + " | " + serviceId + "【FINISH JOB】" + stockNum);
                return true;
            } else {
                System.out.println(user + " | " + serviceId + " ------------ NO LOCK");
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
                System.out.println(user + " | " + serviceId + " ------------ UNLOCK FAILED");
                return false;
            }
        }

    }

}
