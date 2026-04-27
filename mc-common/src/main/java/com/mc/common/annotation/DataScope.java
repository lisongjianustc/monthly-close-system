package com.mc.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 标注在Mapper方法上，自动拼接数据范围过滤条件
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 数据域类型：SELF-本单位、SUBTREE-辖内、ALL-全局
     */
    String value() default "SUBTREE";
}
