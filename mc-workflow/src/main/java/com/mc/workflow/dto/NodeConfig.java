package com.mc.workflow.dto;

import lombok.Data;

/**
 * 节点配置DTO
 */
@Data
public class NodeConfig {

    /** 节点编码 */
    private String code;

    /** 节点名称 */
    private String name;

    /** 节点类型: ONE_TIME_TASK-一次性任务, PERIODIC_TASK-周期任务, IMPORT_CHECK_TASK-导入校验任务, CLOSE_CONFIRM_TASK-关账确认任务 */
    private String type;

    /** 责任人ID */
    private Long assigneeId;

    /** 责任人名称 */
    private String assigneeName;

    /** SLA时长(小时) */
    private Integer slaHours;

    /** 前置节点编码(逗号分隔) */
    private String preNodeCodes;

    /** 显示顺序 */
    private Integer sort;

    /** 是否禁用 */
    private Boolean disabled;
}
