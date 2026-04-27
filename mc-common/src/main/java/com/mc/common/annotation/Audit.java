package com.mc.common.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 标注在Controller方法上，自动记录审计日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    /**
     * 业务类型
     */
    String businessType() default "";

    /**
     * 操作类型
     */
    String action() default "";

    /**
     * 业务ID参数名（从方法参数中获取）
     */
    String bizIdParam() default "id";
}
