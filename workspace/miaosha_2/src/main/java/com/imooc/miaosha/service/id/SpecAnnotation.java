package com.imooc.miaosha.service.id;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpecAnnotation {

    String desc() default "描述信息";

}
