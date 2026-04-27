package com.mc.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务轨迹
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_task_trace")
public class WfTaskTrace extends WorkflowBaseEntity {

    /** 任务ID */
    private Long taskId;

    /** 操作类型: CREATE-创建, START-开始, SUBMIT-提交, APPROVE-复核通过, REJECT-复核退回, APPROVE_FINAL-审核通过, REJECT_FINAL-审核退回, TRANSFER-转交, SLA_EXPIRED-SLA逾期 */
    private String action;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 操作时间 */
    private LocalDateTime operateTime;

    /** 操作备注 */
    private String comment;
}