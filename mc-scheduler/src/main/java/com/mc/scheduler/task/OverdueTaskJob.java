package com.mc.scheduler.task;

import com.mc.workflow.service.IWfTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SLA逾期检测定时任务
 * <p>
 * 每5分钟扫描一次，将PENDING/IN_PROGRESS状态且已超期的任务标记为OVERDUE
 * </p>
 */
@Slf4j
@Component
public class OverdueTaskJob {

    @Autowired
    private IWfTaskService taskService;

    public void execute() {
        log.info("开始执行SLA逾期检测任务");
        try {
            taskService.markOverdueTasks();
            log.info("SLA逾期检测任务执行完成");
        } catch (Exception e) {
            log.error("SLA逾期检测任务执行异常", e);
        }
    }
}
