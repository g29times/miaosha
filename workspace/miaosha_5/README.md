第六章

重点
1 缓存优化 多级缓存
    CDN
    浏览器
    页面/URL(带参数)  GoodsController.list
    对象  MiaoshaUserService.getById
2 动静分离
3 SQL性能优化：更新对象只更新部分属性-MiaoshaUserService.updatePassword(){miaoshaUserDao.update(toBeUpdate)}
4 缓存更新策略
https://www.jianshu.com/p/3c111e4719b8
https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651961319&idx=1&sn=8e683c4ba4cc74330bf19766eb05163b&chksm=bd2d023b8a5a8b2d6672c476bdd4f768b2d112a65cca50bc40a875df1b2620d51ffb4725cde4&scene=0#rd
https://docs.microsoft.com/en-us/previous-versions/msp-n-p/dn589799(v=pandp.10)
5 防止超卖 6-7
    1 SQL 判断
    2 数据库唯一索引