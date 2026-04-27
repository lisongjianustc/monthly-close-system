package com.mc.rule.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 规则配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rule_config")
public class RuleConfig extends RuleBaseEntity {

    /** 规则编码 */
    @JsonProperty("ruleCode")
    private String code;

    /** 规则名称 */
    @JsonProperty("ruleName")
    private String name;

    /** 规则类型: VALIDATE-校验, CALCULATE-计算, FILTER-过滤 */
    private String ruleType;

    /** 规则分类: BALANCE-余额校验, PERIOD-期间校验, AMOUNT-金额校验 */
    private String category;

    /** 状态: DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已停用 */
    private String status;

    /** 规则表达式(Aviator) */
    private String expression;

    /** 规则描述 */
    private String description;

    /** 错误提示信息 */
    private String errorMessage;

    /** 优先级 */
    private Integer priority;
}
