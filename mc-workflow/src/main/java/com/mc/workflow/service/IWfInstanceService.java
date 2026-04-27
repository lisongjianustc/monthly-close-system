package com.mc.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.workflow.entity.WfInstance;
import com.mc.workflow.entity.WfTask;

import java.util.List;

/**
 * 流程实例Service
 */
public interface IWfInstanceService extends IService<WfInstance> {

    /**
     * 实例化流程
     */
    Long instantiate(Long templateId, Long periodId, Long unitId);

    /**
     * 查询期间实例
     */
    List<WfInstance> getByPeriodId(Long periodId);

    /**
     * 查询单位实例
     */
    List<WfInstance> getByUnitId(Long unitId);

    /**
     * 取消实例
     */
    boolean cancelInstance(Long id);

    /**
     * 派发流程实例的任务
     */
    List<WfTask> dispatchTasks(Long instanceId);
}