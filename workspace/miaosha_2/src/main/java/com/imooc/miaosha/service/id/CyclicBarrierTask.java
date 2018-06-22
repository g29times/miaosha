package com.imooc.miaosha.service.id;

import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.lock.StockKey;
import com.imooc.miaosha.util.BeanFactory;

import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTask implements Runnable {

    private static RedisService redisService = BeanFactory.getBean(RedisService.class);

    private CyclicBarrier cyclicBarrier;

    public CyclicBarrierTask(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try {
            // 等待所有线程准备就绪
            cyclicBarrier.await();
            /************************* 业务开始 ****************************/
            redisService.lock(redisService::plus, StockKey.getByNum, "");

//            IdUtil center0worker0 = IdUtil.getInstance(0, 0);
//            // 同时执行测试内容
//            center0worker0.nextId();
//            System.out.println(Thread.currentThread().getName() + " " + service.nextId());
            /************************* 业务结束 ****************************/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
