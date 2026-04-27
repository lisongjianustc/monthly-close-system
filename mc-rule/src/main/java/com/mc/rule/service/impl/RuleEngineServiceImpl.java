package com.mc.rule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.rule.dto.RuleExecuteRequest;
import com.mc.rule.dto.RuleExecuteResult;
import com.mc.rule.entity.RuleConfig;
import com.mc.rule.entity.RuleExecuteLog;
import com.mc.rule.entity.RuleGroup;
import com.mc.rule.mapper.RuleConfigMapper;
import com.mc.rule.mapper.RuleExecuteLogMapper;
import com.mc.rule.mapper.RuleGroupMapper;
import com.mc.rule.service.IRuleEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则引擎Service实现
 */
@Service
public class RuleEngineServiceImpl extends ServiceImpl<RuleConfigMapper, RuleConfig> implements IRuleEngineService {

    @Autowired
    private RuleGroupMapper groupMapper;

    @Autowired
    private RuleExecuteLogMapper logMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /** 表达式编译结果缓存，避免重复编译 */
    private final ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    /** Aviator沙箱配置 — 类加载时执行一次 */
    static {
        // 使用 ALLOWED_CLASS_SET 白名单，仅允许基础数学和字符串类型
        AviatorEvaluator.getInstance()
            .setOption(Options.ALLOWED_CLASS_SET, java.util.Collections.emptySet());  // 空=拒绝所有类访问
    }

    @Override
    public boolean createRule(RuleConfig rule) {
        rule.setStatus("DRAFT");
        return this.save(rule);
    }

    @Override
    public boolean updateRule(RuleConfig rule) {
        RuleConfig existing = this.getById(rule.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "规则不存在");
        }
        if ("PUBLISHED".equals(existing.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "已发布的规则不能修改");
        }
        // 清除缓存
        expressionCache.remove(existing.getExpression());
        return this.updateById(rule);
    }

    @Override
    @Transactional
    public boolean publishRule(Long id) {
        RuleConfig rule = this.getById(id);
        if (rule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "规则不存在");
        }
        if (!validateExpression(rule.getExpression())) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "规则表达式无效: " + rule.getExpression());
        }
        rule.setStatus("PUBLISHED");
        return this.updateById(rule);
    }

    @Override
    public RuleExecuteResult executeRule(RuleExecuteRequest request) {
        long startTime = System.currentTimeMillis();
        RuleExecuteResult result = new RuleExecuteResult();

        try {
            RuleConfig rule = null;
            if (request.getRuleId() != null) {
                rule = this.getById(request.getRuleId());
            } else if (request.getRuleCode() != null && !request.getRuleCode().isEmpty()) {
                rule = this.getOne(new LambdaQueryWrapper<RuleConfig>()
                    .eq(RuleConfig::getCode, request.getRuleCode())
                    .eq(RuleConfig::getStatus, "PUBLISHED"));
            }

            if (rule == null) {
                result.setSuccess(false);
                result.setErrorMsg("规则不存在");
                return result;
            }

            result.setRuleCode(rule.getCode());

            // 从缓存获取或重新编译表达式
            Expression compiledExpr = expressionCache.computeIfAbsent(
                rule.getExpression(),
                expr -> AviatorEvaluator.compile(expr)
            );
            Object execResult = compiledExpr.execute(request.getParams());

            result.setSuccess(true);
            result.setResult(execResult);

            if (Boolean.TRUE.equals(request.getLogExecution())) {
                recordExecutionLog(rule, request.getParams(), execResult, null, System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
            result.setErrorTip("规则执行失败");

            if (Boolean.TRUE.equals(request.getLogExecution())) {
                RuleConfig rule = this.getById(request.getRuleId());
                if (rule != null) {
                    recordExecutionLog(rule, request.getParams(), null, e.getMessage(), System.currentTimeMillis() - startTime);
                }
            }
        }

        result.setExecuteDuration(System.currentTimeMillis() - startTime);
        return result;
    }

    @Override
    public List<RuleExecuteResult> executeRules(List<RuleExecuteRequest> requests) {
        List<RuleExecuteResult> results = new ArrayList<>();
        for (RuleExecuteRequest request : requests) {
            results.add(executeRule(request));
        }
        return results;
    }

    @Override
    public List<RuleExecuteResult> executeRuleGroup(String groupCode, Map<String, Object> params) {
        // 先查出所有规则，一次性加载避免循环查库
        List<RuleConfig> rules = this.list(new LambdaQueryWrapper<RuleConfig>()
            .eq(RuleConfig::getStatus, "PUBLISHED"));

        RuleGroup group = groupMapper.selectList(null).stream()
            .filter(g -> groupCode.equals(g.getCode()) && "PUBLISHED".equals(g.getStatus()))
            .findFirst()
            .orElse(null);

        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "规则组不存在: " + groupCode);
        }

        List<RuleExecuteResult> results = new ArrayList<>();

        try {
            List<Long> ruleIds = objectMapper.readValue(group.getRuleIds(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));

            // 用 Map 一次性加载所有规则，内存比对
            Map<Long, RuleConfig> ruleMap = new HashMap<>();
            for (RuleConfig r : rules) {
                ruleMap.put(r.getId(), r);
            }

            for (Long ruleId : ruleIds) {
                RuleConfig rule = ruleMap.get(ruleId);
                if (rule == null) continue;

                RuleExecuteRequest request = new RuleExecuteRequest();
                request.setRuleId(ruleId);
                request.setParams(params);
                request.setLogExecution(true);
                results.add(executeRule(request));
            }
        } catch (Exception e) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "规则组执行失败: " + e.getMessage());
        }

        return results;
    }

    @Override
    public List<RuleConfig> listRules(String status, String category) {
        LambdaQueryWrapper<RuleConfig> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(RuleConfig::getStatus, status);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(RuleConfig::getCategory, category);
        }
        wrapper.orderByAsc(RuleConfig::getPriority);
        return this.list(wrapper);
    }

    @Override
    public boolean validateExpression(String expression) {
        try {
            AviatorEvaluator.compile(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void recordExecutionLog(RuleConfig rule, Map<String, Object> params, Object result, String errorMsg, long duration) {
        try {
            RuleExecuteLog log = new RuleExecuteLog();
            log.setRuleId(rule.getId());
            log.setRuleCode(rule.getCode());
            log.setRuleName(rule.getName());
            log.setExecuteTime(LocalDateTime.now());
            log.setResult(errorMsg == null ? "SUCCESS" : "FAIL");
            log.setInputParams(objectMapper.writeValueAsString(params));
            log.setOutput(result != null ? result.toString() : null);
            log.setErrorMsg(errorMsg);
            log.setExecuteDuration(duration);
            logMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
        }
    }
}
