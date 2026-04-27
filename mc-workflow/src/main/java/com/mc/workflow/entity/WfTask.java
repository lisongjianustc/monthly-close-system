package com.mc.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务实例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_task")
public class WfTask extends WorkflowBaseEntity {

    /** 流程实例ID */
    private Long instanceId;

    /** 节点编码 */
    private String nodeCode;

    /** 节点名称 */
    private String nodeName;

    /** 节点类型: ONE_TIME_TASK-一次性任务, PERIODIC_TASK-周期任务, IMPORT_CHECK_TASK-导入校验任务, CLOSE_CONFIRM_TASK-关账确认任务 */
    private String nodeType;

    /** 任务状态: PENDING-待处理, IN_PROGRESS-进行中, PENDING_REVIEW-待复核, PENDING_APPROVE-待审核, COMPLETED-已完成, REJECTED-已退回 */
    private String status;

    /** 责任人ID */
    private Long assigneeId;

    /** 责任人名称 */
    private String assigneeName;

    /** SLA截止时间 */
    private LocalDateTime slaDeadline;

    /** 实际开始时间 */
    private LocalDateTime startTime;

    /** 实际结束时间 */
    private LocalDateTime endTime;

    /** 前置节点ID(逗号分隔) */
    private String preTaskIds;

    /** 显示顺序 */
    private Integer sort;
}