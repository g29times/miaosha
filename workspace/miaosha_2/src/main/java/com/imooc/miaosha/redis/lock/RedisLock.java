package com.imooc.miaosha.redis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.Collections;

import static com.imooc.miaosha.redis.RedisService.returnToPool;

/**
 * https://www.cnblogs.com/linjiqin/p/8003838.html
 * https://www.cnblogs.com/0201zcr/p/5942748.html
 */
@Component
public class RedisLock {

    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static final Long RELEASE_SUCCESS = 1L;
    private static final String LOCK_SUCCESS = "OK";

    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    // 优化 改为静态
    // Jedis jedis, String lockKey, String lockVal

    /**
     * 尝试获取分布式锁
     *
     * @param jedis      Redis客户端
     * @param lockKey    锁
     * @param requester  请求者
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryLock(Jedis jedis, String lockKey, String requester, int expireTime) {
        try {
            String result = jedis.set(lockKey, requester, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 尝试释放分布式锁
     *
     * @param jedis     Redis客户端
     * @param lockKey   锁
     * @param requester 请求者
     * @return 是否释放成功
     */
    public static boolean tryUnlock(Jedis jedis, String lockKey, String requester) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requester));
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            returnToPool(jedis);
        }
    }
}
