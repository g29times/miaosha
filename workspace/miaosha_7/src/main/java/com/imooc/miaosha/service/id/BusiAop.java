package com.imooc.miaosha.service.id;

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
 */
@Aspect
@Component
public class BusiAop {

    private Logger logger = LoggerFactory.getLogger(BusiAop.class);

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
    public Object aroundId(ProceedingJoinPoint pjd, SpecAnnotation spec) throws Throwable {
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
