package com.imooc.miaosha.config;

import com.google.gson.Gson;
import com.imooc.miaosha.util.id.SpecAnnotation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 切面工具类
 * <p>
 * 1.@Component必须
 * 2.jrebel重载无效
 * 3.static无效 内部类无效?
 * <p>
 * Spring AOP缺点和不足
 * https://blog.csdn.net/garfielder007/article/details/78057107
 * 高级 流程切面
 * https://blog.csdn.net/yangshangwei/article/details/77422296
 * 高级 复合切点切面
 * https://blog.csdn.net/yangshangwei/article/details/77429625
 */
@Aspect
@Component
public class BusiAop {

    private Logger logger = LoggerFactory.getLogger(BusiAop.class);

    // ***************************** 定义切点

    /**
     * 通用切点
     */
    @Pointcut("execution(* com.imooc.miaosha..*.*(..))")
    public void commonPointcut() {
    }

    /**
     * controller切点
     */
    @Pointcut("execution(* com.imooc.miaosha.controller..*.*(..))")
    public void ctrlPointcut() {
    }

    // ***************************** 定义AOP环绕通知

    /**
     * controller层通知
     *
     * @param pjp 连接点
     * @return
     * @throws Throwable
     */
    @Around("ctrlPointcut()")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        return doLog(pjp, false);
    }

    /**
     * 通用+定制条件通知(自定义注解方式)
     *
     * @param pjp  连接点
     * @param spec
     * @return
     * @throws Throwable
     */
    @Around("commonPointcut() && @annotation(spec)")
    public Object aroundSpec(ProceedingJoinPoint pjp, SpecAnnotation spec) throws Throwable {
        logger.info("LOG_DESC : {}", spec.desc());
        return doLog(pjp, true);
    }

    // ***************************** 日志作业抽取

    /**
     * @param pjp
     * @param isDurable 是否需要持久化
     * @return
     * @throws Throwable
     */
    private Object doLog(ProceedingJoinPoint pjp, boolean isDurable) throws Throwable {
        Gson gson = new Gson();
        StringBuffer METHOD_PARAMS = new StringBuffer("{");

        // 方法部分
        String METHOD_NAME = pjp.getSignature().toShortString();
        String[] METHOD_PARAM_NAMES = ((MethodSignature) pjp.getSignature()).getParameterNames();
        Object[] METHOD_PARAM_VALUES = pjp.getArgs();
        Object METHOD_RESULT = null;
        for (int i = 0; i < METHOD_PARAM_VALUES.length; i++) {
            String name = METHOD_PARAM_NAMES[i];
            Object arg = METHOD_PARAM_VALUES[i];
            if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
                continue;
            }
            METHOD_PARAMS.append("\"").append(name).append("\" : ")
                    .append(gson.toJson(arg)).append(", ");
        }
        if (METHOD_PARAMS.length() > 1) {
            METHOD_PARAMS.deleteCharAt(METHOD_PARAMS.length() - 2);
            METHOD_PARAMS.append("}");
        } else { // 方法没有参数
            METHOD_PARAMS.deleteCharAt(0);
        }

        // request部分
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
        String REQUEST_URL = request.getRequestURL().toString();
        String REQUEST_METHOD = request.getMethod();
        String FULL_URL = REQUEST_METHOD + " " + REQUEST_URL;
        logger.debug("请求开始, url: {}, method: {}, params: {}",
                FULL_URL, METHOD_NAME, METHOD_PARAMS);

        // 执行
        try {
            METHOD_RESULT = pjp.proceed();
            logger.debug("请求结束，controller的返回值是 : {}", METHOD_RESULT);
        } catch (Throwable e) {
//            logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw e;
        }

        // persist log
//        db.save(log);
//        redis.save(log);
        return METHOD_RESULT;
    }

    // ***************************** 消息作业抽取

    private Object doMessage(ProceedingJoinPoint pjp) throws Throwable {
        // TODO
        return null;
    }

    // ***************************** 缓存作业抽取

    private Object doCache(ProceedingJoinPoint pjp) throws Throwable {
        // TODO
        return null;
    }

    // ***************************** 异常作业抽取

    private Object doException(ProceedingJoinPoint pjp) throws Throwable {
        // TODO
        return null;
    }
}
