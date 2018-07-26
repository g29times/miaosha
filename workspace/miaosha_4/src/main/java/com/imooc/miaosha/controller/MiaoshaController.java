package com.imooc.miaosha.controller;

import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.RedisLock;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static com.imooc.miaosha.redis.RedisService.returnToPool;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    JedisPool jedisPool;

    /**
     * QPS:1306
     * 5000 * 10
     */
    @RequestMapping("/do_miaosha")
    public String list(Model model, MiaoshaUser user,
                       @RequestParam("goodsId") long goodsId) {
//        System.out.println(user.getId());

        model.addAttribute("user", user);
        if (user == null) {
            return "login";
        }


        Jedis jedis = null;
        String serviceId = Thread.currentThread().getName();
        String lockKey = "MIAOSHA";
        try {
            jedis = jedisPool.getResource();
            if (RedisLock.tryLock(jedis, lockKey, serviceId, 30000)) {
                System.out.println(user.getId() + " | " + serviceId + "【GET LOCK】");


                //判断库存
                GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
                int stock = goods.getStockCount();
                if (stock <= 0) {
                    model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
                    System.out.println(user.getId() + " | " + serviceId + "没有库存");
                    return "miaosha_fail";
                }
                //判断是否已经秒杀到了
                MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
                if (order != null) {
                    model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
                    System.out.println(user.getId() + " | " + serviceId + "已经秒杀");
                    return "miaosha_fail";
                }
                //减库存 下订单 写入秒杀订单
                OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
                model.addAttribute("orderInfo", orderInfo);
                model.addAttribute("goods", goods);
                System.out.println(user.getId() + " | " + serviceId + "【FINISH JOB】");
                return "order_detail";


            } else {
                return "miaosha_fail";
            }
        } catch (Exception e) {
            return "miaosha_fail";
        } finally {
            returnToPool(jedis);
            if (RedisLock.tryUnlock(jedis, lockKey, serviceId)) {
                System.out.println(user.getId() + " | " + serviceId + "【UNLOCK】");
            } else {
                return "miaosha_fail";
            }
        }


    }


    private static int stock = 5;

    @ResponseBody
    @RequestMapping("/testLock")
    public String list(String userId) {
        Jedis jedis = null;
        String serviceId = Thread.currentThread().getName();
        String lockKey = "MIAOSHA";
        try {
            jedis = jedisPool.getResource();
            if (RedisLock.tryLock(jedis, lockKey, serviceId, 30000)) {
                System.out.println(userId + " | " + serviceId + "【GET LOCK】");
//                Thread.sleep(50);
                if (stock >= 0) {
                    stock--;
//                    Thread.sleep(50);
                    System.out.println("【STOCK】=" + stock);
                    return "success";
                } else {
                    return "fail";
                }
            } else {
                return "fail";
            }
        } catch (Exception e) {
            return "fail";
        } finally {
            returnToPool(jedis);
            if (RedisLock.tryUnlock(jedis, lockKey, serviceId)) {
                System.out.println(userId + " | " + serviceId + "【UNLOCK】");
            } else {
                return "fail";
            }
        }
    }

}
