package com.imooc.miaosha.util;

import com.imooc.miaosha.util.id.IdTask;
import com.imooc.miaosha.util.id.SpecAnnotation;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

/**
 * @author Li Tong
 * <p>
 * Twitter_Snowflake<br>
 * SnowFlake的结构如下(每部分用-分开):<br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 <br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0<br>
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId<br>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 * 加起来刚好64位，为一个Long型。<br>
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 */
@Component
public class IdUtil {

    // ==============================Fields===========================================
    /**
     * 开始时间截 (2018/6/19) 支持往后使用69年
     */
    private final long twepoch = 1529560800000L;

    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 5L;

    /**
     * 数据标识id所占的位数
     */
    private final long datacenterIdBits = 5L;

    /**
     * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * 支持的最大数据标识id，结果是31
     */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 12L;

    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;

    /**
     * 数据标识id向左移17位(12+5)
     */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间截向左移22位(5+5+12)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作机器ID(0~31)
     */
    private long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * 业务类型
     */
    private Class business;

    /**
     * 业务描述
     */
    private String bussDesc;

    //==============================Constructors=====================================

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    private IdUtil(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    private IdUtil() {
    }

    // 单例
    private static class NestClass {
        private static IdUtil instance = new IdUtil(0, 0);
    }

    @SpecAnnotation(desc = "获得默认环境的分布式ID")
    public static IdUtil getInstance() {
        return NestClass.instance;
    }

    @SpecAnnotation(desc = "获得指定环境的分布式ID")
    public static IdUtil getInstance(long workerId, long datacenterId) {
        NestClass.instance = new IdUtil(workerId, datacenterId);
        return NestClass.instance;
    }

    // ==============================Methods==========================================

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) //
                | (datacenterId << datacenterIdShift) //
                | (workerId << workerIdShift) //
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        // Instant.now().getEpochSecond()
        return System.currentTimeMillis();
    }

    //==============================Test=============================================

    /**
     * 测试
     */
    public static void main(String[] args) {
        // 并发测试 结果：4核心8线程i7处理器 每秒可支持3000以上绝对并发
//        long start = System.currentTimeMillis();
//        conTest();
//        long end = System.currentTimeMillis();
//        System.out.println(end - start);

        // 吞吐测试 结果：每秒可产生3000000个id
        long start = System.currentTimeMillis();
        int count = 100000;
        throughputTest(count);
        long end = System.currentTimeMillis();
        // 每秒产生3000000个
        System.out.println((1000 / (end - start)) * count);
    }

    /**
     * 并发测试
     * 模拟count个并发
     */
    private static void conTest() {
        int count = 300;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
        ExecutorService executorService = new ThreadPoolExecutor(count, count,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < count; i++) {
            executorService.execute(new IdTask(cyclicBarrier));
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 吞吐测试
     */
    private static void throughputTest(int count) {
        // 92233 72036 85477 5807
        System.out.println("Long.MAX_VALUE = " + Long.MAX_VALUE);
        // 数据中心0 主机0
        IdUtil center0worker0 = IdUtil.getInstance(0, 0)/*new IdUtil(0, 0)*/;
        // 数据中心0 主机1
        IdUtil center0worker1 = IdUtil.getInstance(1, 0)/*new IdUtil(1, 0)*/;
        // 数据中心1 主机0
        IdUtil center1worker0 = IdUtil.getInstance(0, 1)/*new IdUtil(0, 1)*/;
        long id00 = 0L, id01 = 0L, id10 = 0L;
        // 生成id
        for (int i = 0; i < count; i++) {
            id00 = center0worker0.nextId();
            id01 = center0worker1.nextId();
            id10 = center1worker0.nextId();
        }
        System.out.println("id00 = " + id00);
        System.out.println("id01 = " + id01);
        System.out.println("id10 = " + id10);
        // 原始数据
        String bin00 = Long.toBinaryString(id00);
        String bin01 = Long.toBinaryString(id01);
        String bin10 = Long.toBinaryString(id10);
        System.out.println("Binary id00 = " + bin00);
        System.out.println("Binary id01 = " + bin01);
        System.out.println("Binary id10 = " + bin10);

        System.out.println();

        // 以下只显示00信息
//        10 0100 0001 0000 0000 1010  00000 00000  0100 0110 0001 | for100000000 = 9912826463329
//        10 0001 0101 0100 0100 1011  00000 00000  0001 1100 0111 | for10000000  = 10687887706360
//        10 0111 0010 1011 0010 0011  00000 00000  0001 1111 1011 | for1000000   = 10766556070395

        // 生成id距离当前时间
        String time = bin00.substring(0, bin00.length() - 22);
        System.out.println("Binary time = " + time);
        Long t = Long.valueOf(time, 2);
        System.out.println("Id time = " + t);
        // 对比 生成id距离当前时间 和 实际距离当前时间 是否一致
        System.out.println("Check time = " + (System.currentTimeMillis() - center0worker0.twepoch));
        // 转化为可读时间
        System.out.println("从时间原点到生成id过去了" + Duration.between(Instant.ofEpochMilli(center0worker0.twepoch), Instant.now()).toMinutes() + "分钟");
    }

}
