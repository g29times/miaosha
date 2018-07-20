第8章
限流

1 注意WebConfig的使用
了解如何注入ArgumentResolver，Converter，Interceptor，Filter
2 用户信息上下文如何传递
使用ThreadLocal保存UserContext
在AccessInterceptor中保存
在UserArgumentResolver中提取
3 Aop中如何获取request，response？
一般拦截器或者过滤器会提供，但aop中没有，是怎么解决的？
思路是怎样？ 答案是延续第二条，使用ThreadLocal
自己实现：https://www.cnblogs.com/qlong8807/p/7573175.html
Spring实现：https://blog.csdn.net/kid_2412/article/details/52180657