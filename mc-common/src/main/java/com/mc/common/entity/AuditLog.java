package com.mc.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Data
@TableName("audit_log")
public class AuditLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 操作类型 */
    private String action;

    /** 业务类型 */
    private String businessType;

    /** 业务ID */
    private Long bizId;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 方法名 */
    private String methodName;

    /** 类名 */
    private String className;

    /** 请求参数（JSON） */
    private String requestParams;

    /** 执行时长（毫秒） */
    private Long executionTimeMs;

    /** IP地址 */
    private String ipAddress;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
