package com.mc.rule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.rule.dto.RuleExecuteRequest;
import com.mc.rule.dto.RuleExecuteResult;
import com.mc.rule.entity.RuleConfig;
import com.mc.rule.entity.RuleExecuteLog;

import java.util.List;

/**
 * 规则引擎Service
 */
public interface IRuleEngineService extends IService<RuleConfig> {

    /**
     * 创建规则
     */
    boolean createRule(RuleConfig rule);

    /**
     * 更新规则
     */
    boolean updateRule(RuleConfig rule);

    /**
     * 发布规则
     */
    boolean publishRule(Long id);

    /**
     * 执行单个规则
     */
    RuleExecuteResult executeRule(RuleExecuteRequest request);

    /**
     * 批量执行规则
     */
    List<RuleExecuteResult> executeRules(List<RuleExecuteRequest> requests);

    /**
     * 按规则组执行规则
     */
    List<RuleExecuteResult> executeRuleGroup(String groupCode, java.util.Map<String, Object> params);

    /**
     * 查询规则列表
     */
    List<RuleConfig> listRules(String status, String category);

    /**
     * 校验规则表达式
     */
    boolean validateExpression(String expression);
}
