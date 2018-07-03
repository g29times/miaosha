package com.imooc.miaosha.util.id;

import com.imooc.miaosha.util.IdUtil;

import java.util.concurrent.CyclicBarrier;

public class IdTask implements Runnable {

    private CyclicBarrier cyclicBarrier;

    public IdTask(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(30000);
            // 等待所有线程准备就绪
            cyclicBarrier.await();
            IdUtil center0worker0 = IdUtil.getInstance(0, 0);
            // 同时执行测试内容
            center0worker0.nextId();
//            System.out.println(Thread.currentThread().getName() + " " + service.nextId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
