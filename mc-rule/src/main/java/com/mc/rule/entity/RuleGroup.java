package com.mc.rule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 规则组配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rule_group")
public class RuleGroup extends RuleBaseEntity {

    /** 规则组编码 */
    private String code;

    /** 规则组名称 */
    private String name;

    /** 规则组类型: IMPORT-导入校验, WORKFLOW-流程校验, REPORT-报表校验 */
    private String groupType;

    /** 状态: DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已停用 */
    private String status;

    /** 规则ID列表(JSON) */
    private String ruleIds;

    /** 规则执行顺序 */
    private String executionOrder;

    /** 描述 */
    private String description;
}
