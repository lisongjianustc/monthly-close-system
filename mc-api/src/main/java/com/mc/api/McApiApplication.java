package com.mc.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 月结报表管理系统 - API启动类
 */
@SpringBootApplication(scanBasePackages = {"com.mc.api", "com.mc.api.config", "com.mc.system", "com.mc.workflow", "com.mc.data", "com.mc.rule", "com.mc.exception", "com.mc.notification", "com.mc.common.service"})
@MapperScan({"com.mc.system.mapper", "com.mc.workflow.mapper", "com.mc.data.mapper", "com.mc.rule.mapper", "com.mc.exception.mapper", "com.mc.notification.mapper", "com.mc.common.mapper"})
public class McApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(McApiApplication.class, args);
    }
}