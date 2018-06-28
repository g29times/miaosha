第四章笔记

1 order表冗余了goods_name属性，以便列表展示
2 生产环境秒杀价格没有小数 都是整数 用“分”表示
3 id - snowflake
4 减库存 下订单 写入秒杀订单 必须事务
5 编程规范 MiaoshaService 引入其他Service而不是Dao