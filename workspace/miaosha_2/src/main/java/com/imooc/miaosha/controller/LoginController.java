package com.imooc.miaosha.controller;

import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.lock.StockKey;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.util.ConcurrentUtil;
import com.imooc.miaosha.util.id.SpecAnnotation;
import com.imooc.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        //登录
        userService.login(response, loginVo);
        return Result.success(true);
    }


    @GetMapping("/lock/{threadNum}")
    @ResponseBody
    @SpecAnnotation(desc = "testlock")
    public Object lockModify(@PathVariable Integer threadNum) throws Exception {

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

}
