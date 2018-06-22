package com.imooc.miaosha.redis.lock;

import com.imooc.miaosha.redis.BasePrefix;

public class StockKey extends BasePrefix {

    private StockKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static StockKey getByNum = new StockKey(-1, "stockNum");

}
