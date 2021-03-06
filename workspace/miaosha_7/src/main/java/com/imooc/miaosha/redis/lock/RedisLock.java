package com.imooc.miaosha.redis.lock;

import com.imooc.miaosha.service.id.SpecAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.Collections;

@Component
public class RedisLock {

    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static final Long RELEASE_SUCCESS = 1L;
    private static final String LOCK_SUCCESS = "OK";

    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    // 优化 改为静态
//    Jedis jedis, String lockKey, String lockVal

    /**
     * 尝试获取分布式锁
     *
     * @param jedis      Redis客户端
     * @param lockKey    锁
     * @param requester  请求者
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    @SpecAnnotation(desc = "尝试获取分布式锁")
    public static boolean tryLock(Jedis jedis, String lockKey, String requester, int expireTime) {
        System.out.println(Thread.currentThread().getName() + " try lock");
        String result = jedis.set(lockKey, requester, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 尝试释放分布式锁
     *
     * @param jedis     Redis客户端
     * @param lockKey   锁
     * @param requester 请求者
     * @return 是否释放成功
     */
    @SpecAnnotation(desc = "尝试释放分布式锁")
    public static boolean tryUnlock(Jedis jedis, String lockKey, String requester) {
        System.out.println(Thread.currentThread().getName() + " try unlock");
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requester));
        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}
