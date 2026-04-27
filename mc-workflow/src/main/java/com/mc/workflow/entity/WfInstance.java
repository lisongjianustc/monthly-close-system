package com.mc.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程实例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_instance")
public class WfInstance extends WorkflowBaseEntity {

    /** 模板ID */
    private Long templateId;

    /** 期间ID */
    private Long periodId;

    /** 单位ID */
    private Long unitId;

    /** 实例状态: ACTIVE-进行中, COMPLETED-已完成, CANCELLED-已取消 */
    private String status;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;
}