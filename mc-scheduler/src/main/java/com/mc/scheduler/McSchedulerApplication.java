package com.mc.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.mc.scheduler", "com.mc.workflow", "com.mc.common"})
@EnableScheduling
public class McSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McSchedulerApplication.class, args);
    }
}
