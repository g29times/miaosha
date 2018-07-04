package com.imooc.miaosha.util;

import com.imooc.miaosha.util.id.CyclicBarrierTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * https://www.cnblogs.com/uodut/p/6830939.html
 * https://blog.csdn.net/BDblog_chang/article/details/71076098
 */
public class ConcurrentUtil {

    private static Logger logger = LoggerFactory.getLogger(ConcurrentUtil.class);

    /**
     * 并发测试
     * 模拟threadNum个并发
     */
    public static void conTest(int threadNum) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadNum);
        ExecutorService executorService = new ThreadPoolExecutor(threadNum, threadNum,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < threadNum; i++) {
            executorService.execute(new CyclicBarrierTask(cyclicBarrier));
        }
        executorService.shutdown();
        // 判断是否所有的线程已经运行完
        while (!executorService.isTerminated()) {
            try {
                // 所有线程池中的线程执行完毕，执行后续操作
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
