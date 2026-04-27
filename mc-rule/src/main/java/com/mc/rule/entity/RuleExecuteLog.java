package com.mc.rule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 规则执行日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rule_execute_log")
public class RuleExecuteLog extends RuleBaseEntity {

    /** 规则ID */
    private Long ruleId;

    /** 规则编码 */
    private String ruleCode;

    /** 规则名称 */
    private String ruleName;

    /** 执行时间 */
    private LocalDateTime executeTime;

    /** 执行结果: SUCCESS-成功, FAIL-失败 */
    private String result;

    /** 输入参数(JSON) */
    private String inputParams;

    /** 执行输出 */
    private String output;

    /** 错误信息 */
    private String errorMsg;

    /** 执行时长(ms) */
    private Long executeDuration;
}
