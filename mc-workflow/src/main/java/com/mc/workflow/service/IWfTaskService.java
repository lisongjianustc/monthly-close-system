package com.mc.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.workflow.entity.WfTask;
import com.mc.workflow.entity.WfTaskTrace;

import java.util.List;

/**
 * 任务实例Service
 */
public interface IWfTaskService extends IService<WfTask> {

    /**
     * 创建任务
     */
    Long createTask(Long instanceId, String nodeCode, String nodeName, String nodeType, Long assigneeId, String assigneeName, Integer slaHours);

    /**
     * 开始任务
     */
    boolean startTask(Long taskId);

    /**
     * 提交任务
     */
    boolean submitTask(Long taskId, String comment);

    /**
     * 复核通过
     */
    boolean approveTask(Long taskId, String comment);

    /**
     * 复核退回
     */
    boolean rejectTask(Long taskId, String comment);

    /**
     * 查询待办
     */
    List<WfTask> getPendingTasks(Long assigneeId);

    /**
     * 检查前置任务是否完成
     */
    boolean checkPreTasks(Long taskId);

    /**
     * 查询任务轨迹
     */
    List<WfTaskTrace> getTaskTraces(Long taskId);

    /**
     * 查询逾期任务
     */
    List<WfTask> getOverdueTasks();

    /**
     * 更新任务状态为逾期
     */
    void markOverdueTasks();
}