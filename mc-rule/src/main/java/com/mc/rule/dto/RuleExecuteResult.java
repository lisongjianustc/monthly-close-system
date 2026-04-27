package com.mc.rule.dto;

import lombok.Data;

/**
 * 规则执行结果DTO
 */
@Data
public class RuleExecuteResult {

    /** 是否成功 */
    private Boolean success;

    /** 规则编码 */
    private String ruleCode;

    /** 执行结果 */
    private Object result;

    /** 错误信息 */
    private String errorMsg;

    /** 错误提示 */
    private String errorTip;

    /** 执行时长(ms) */
    private Long executeDuration;

    /** 是否跳过 */
    private Boolean skipped;
}
