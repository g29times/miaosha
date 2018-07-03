package com.imooc.miaosha.config;

import com.imooc.miaosha.util.id.SpecAnnotation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 1.@Component必须
 * 2.jrebel重载无效
 * 3.static无效 内部类无效?
 */
@Aspect
@Component
public class BusiAop {

    private Logger logger = LoggerFactory.getLogger(BusiAop.class);

    /**
     * controller切面
     */
    @Pointcut("execution(* com.imooc.miaosha.controller..*.*(..))")
    public void ctrlPointcut() {
    }

    /**
     * controller
     * @param pjd
     * @return
     * @throws Throwable
     */
    @Around("ctrlPointcut()")
    public Object aroundController(ProceedingJoinPoint pjd) throws Throwable {
        String METHOD_NAME = pjd.getSignature().toLongString();
        String METHOD_PARAM = Arrays.toString(pjd.getArgs());
        Object METHOD_RESULT;
        try {
            logger.info("METHOD_NAME : {}", METHOD_NAME);
            logger.info("METHOD_PARAM : {}", METHOD_PARAM);
            METHOD_RESULT = pjd.proceed();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        return METHOD_RESULT;
    }

    /**
     * 通用切面
     */
    @Pointcut("execution(* com.imooc.miaosha..*.*(..))")
    public void commonPointcut() {
    }

    /**
     * 拦截通用+定制条件(自定义注解)
     * @param pjd
     * @param spec
     * @return
     * @throws Throwable
     */
    @Around("commonPointcut() && @annotation(spec)")
    public Object aroundSpec(ProceedingJoinPoint pjd, SpecAnnotation spec) throws Throwable {
        logger.info("METHOD_DESC : {}", spec.desc());
        String METHOD_NAME = pjd.getSignature().toShortString();
        String METHOD_PARAM = Arrays.toString(pjd.getArgs());
        Object METHOD_RESULT;
        try {
            logger.info("METHOD_NAME : {}", METHOD_NAME);
            logger.info("METHOD_PARAM : {}", METHOD_PARAM);
            METHOD_RESULT = pjd.proceed();
        } catch (Throwable e) {
//            e.printStackTrace();
            logger.error(e.getMessage(), e);
            throw e;
        }
//        logger.info("METHOD_RESULT : {}", METHOD_RESULT);
        return METHOD_RESULT;
    }

}
