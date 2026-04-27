package com.mc.rule.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.rule.dto.RuleExecuteRequest;
import com.mc.rule.dto.RuleExecuteResult;
import com.mc.rule.entity.RuleConfig;
import com.mc.rule.service.IRuleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 规则引擎Controller
 */
@Tag(name = "规则引擎管理")
@RestController
@RequestMapping("/rule")
public class RuleController {

    @Autowired
    private IRuleEngineService ruleEngineService;

    @Audit(action = "创建规则")
    @Operation(summary = "创建规则")
    @PostMapping
    public Result<Boolean> create(@RequestBody RuleConfig rule) {
        return Result.success(ruleEngineService.createRule(rule));
    }

    @Audit(action = "更新规则")
    @Operation(summary = "更新规则")
    @PutMapping
    public Result<Boolean> update(@RequestBody RuleConfig rule) {
        return Result.success(ruleEngineService.updateRule(rule));
    }

    @Audit(action = "发布规则")
    @Operation(summary = "发布规则")
    @PostMapping("/publish/{id}")
    public Result<Boolean> publish(@PathVariable Long id) {
        return Result.success(ruleEngineService.publishRule(id));
    }

    @Operation(summary = "查询规则列表")
    @GetMapping("/list")
    public Result<List<RuleConfig>> list(@RequestParam(required = false) String status,
                                          @RequestParam(required = false) String category) {
        return Result.success(ruleEngineService.listRules(status, category));
    }

    @Operation(summary = "执行规则")
    @PostMapping("/execute")
    public Result<RuleExecuteResult> execute(@RequestBody RuleExecuteRequest request) {
        return Result.success(ruleEngineService.executeRule(request));
    }

    @Operation(summary = "批量执行规则")
    @PostMapping("/execute/batch")
    public Result<List<RuleExecuteResult>> executeBatch(@RequestBody List<RuleExecuteRequest> requests) {
        return Result.success(ruleEngineService.executeRules(requests));
    }

    @Operation(summary = "按规则组执行")
    @PostMapping("/execute/group/{groupCode}")
    public Result<List<RuleExecuteResult>> executeGroup(@PathVariable String groupCode,
                                                       @RequestBody Map<String, Object> params) {
        return Result.success(ruleEngineService.executeRuleGroup(groupCode, params));
    }

    @Operation(summary = "校验表达式")
    @PostMapping("/validate")
    public Result<Boolean> validate(@RequestParam String expression) {
        return Result.success(ruleEngineService.validateExpression(expression));
    }
}
