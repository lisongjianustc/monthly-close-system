package com.mc.exception.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 业务异常记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("business_exception")
public class BusinessExceptionRecord extends ExceptionBaseEntity {

    /** 异常编码 */
    private String exceptionCode;

    /** 异常类型: VALIDATE-校验异常, PROCESS-流程异常, DATA-数据异常, SYSTEM-系统异常 */
    private String exceptionType;

    /** 异常级别: WARN-警告, ERROR-错误, CRITICAL-严重 */
    private String severity;

    /** 关联模块 */
    private String module;

    /** 关联业务ID */
    private String businessId;

    /** 期间ID */
    private Long periodId;

    /** 单位ID */
    private Long unitId;

    /** 异常消息 */
    private String message;

    /** 异常堆栈 */
    private String stackTrace;

    /** 处理状态: PENDING-待处理, HANDLING-处理中, RESOLVED-已解决, IGNORED-已忽略 */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理人名称 */
    private String handlerName;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 处理备注 */
    private String handleComment;

    /** 解决时间 */
    private LocalDateTime resolveTime;
}
