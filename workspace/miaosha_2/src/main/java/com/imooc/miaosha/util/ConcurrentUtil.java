package com.imooc.miaosha.util;

import com.imooc.miaosha.service.id.CyclicBarrierTask;

import java.util.concurrent.*;

/**
 * https://www.cnblogs.com/uodut/p/6830939.html
 * https://blog.csdn.net/BDblog_chang/article/details/71076098
 */
public class ConcurrentUtil {

    /**
     * 并发测试
     * 模拟count个并发
     */
    public static void conTest(int count) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
        ExecutorService executorService = new ThreadPoolExecutor(count, count,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < count; i++) {
            executorService.execute(new CyclicBarrierTask(cyclicBarrier));
        }
        executorService.shutdown();
        // 判断是否所有的线程已经运行完
        while (!executorService.isTerminated()) {
            try {
                // 所有线程池中的线程执行完毕，执行后续操作
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
