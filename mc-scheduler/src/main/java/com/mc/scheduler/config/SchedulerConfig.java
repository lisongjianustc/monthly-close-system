package com.mc.scheduler.config;

import com.mc.scheduler.task.OverdueTaskJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class SchedulerConfig {

    @Autowired
    private OverdueTaskJob overdueTaskJob;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void overdueTaskScan() {
        log.debug("SLA overdue scan triggered");
        overdueTaskJob.execute();
    }
}
