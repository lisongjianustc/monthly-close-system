package com.mc.rule.dto;

import lombok.Data;

import java.util.Map;

/**
 * 规则执行请求DTO
 */
@Data
public class RuleExecuteRequest {

    /** 规则编码 */
    private String ruleCode;

    /** 规则ID */
    private Long ruleId;

    /** 执行参数 */
    private Map<String, Object> params;

    /** 是否记录执行日志 */
    private Boolean logExecution;
}
