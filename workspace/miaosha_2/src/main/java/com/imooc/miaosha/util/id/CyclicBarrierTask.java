package com.imooc.miaosha.util.id;

import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.StockKey;
import com.imooc.miaosha.util.BeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(CyclicBarrierTask.class);

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
            // 同时执行测试内容
            redisService.lock(redisService::plus, StockKey.getByNum, "");
            /************************* 业务结束 ****************************/
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
