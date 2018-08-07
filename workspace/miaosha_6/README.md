第7章笔记
消息队列异步下单

0 使用UserUtil初始化user信息
1 7-4 rabbit四种exchange模式
  https://www.cnblogs.com/zhangweizhong/p/5713874.html
2 7-5 InitializingBean
  Util 系统初始化对Bean修改 类似@postconstruct
3 业务细节
  减库存需要注意判断是否成功
  goodsService.reduceStock(goods);
4 内存标记
  由于redis预减库存会导致redis库存数变为负值，
  这里可以用内存标记优化，0以后不再访问redis
5 压测
  机器硬件和已开进程对压测效果有极大影响，应尽量关闭无关进程，同时减少绝对并发数。
6 7-3 MQ access control
7 启动报错
  1 发送 https://blog.csdn.net/a286352250/article/details/53158667
  2 接受 https://blog.csdn.net/u014513883/article/details/77907898
8 boot配置
  https://blog.csdn.net/zlp1992/article/details/79408989
9 死信队列
  dead-letter-exchange https://www.rabbitmq.com/dlx.html
  
