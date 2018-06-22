package com.imooc.miaosha.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.imooc.miaosha.redis.StockKey;
import com.imooc.miaosha.redis.lock.RedisLock;
import com.imooc.miaosha.service.id.SpecAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.vo.LoginVo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/login")
public class LoginController {

	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }
    
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
    	log.info(loginVo.toString());
    	//登录
    	String token = userService.login(response, loginVo);
    	return Result.success(token);
    }

    @GetMapping("/lock/modify")
    @ResponseBody
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
        return "modify stockNumber success " + stockNumber;
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
