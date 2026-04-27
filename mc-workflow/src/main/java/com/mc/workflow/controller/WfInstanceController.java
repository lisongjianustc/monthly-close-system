package com.mc.workflow.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.workflow.entity.WfInstance;
import com.mc.workflow.service.IWfInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程实例Controller
 */
@Tag(name = "流程实例管理")
@RestController
@RequestMapping("/workflow/instance")
public class WfInstanceController {

    @Autowired
    private IWfInstanceService instanceService;

    @Audit(action = "实例化流程")
    @Operation(summary = "实例化流程")
    @PostMapping("/instantiate")
    public Result<Long> instantiate(@RequestParam Long templateId,
                                    @RequestParam Long periodId,
                                    @RequestParam Long unitId) {
        Long instanceId = instanceService.instantiate(templateId, periodId, unitId);
        return Result.success(instanceId);
    }

    @Operation(summary = "查询期间实例")
    @GetMapping("/period/{periodId}")
    public Result<List<WfInstance>> getByPeriod(@PathVariable Long periodId) {
        return Result.success(instanceService.getByPeriodId(periodId));
    }

    @Operation(summary = "查询实例列表")
    @GetMapping("/list")
    public Result<List<WfInstance>> list() {
        return Result.success(instanceService.list());
    }

    @Operation(summary = "查询单位实例")
    @GetMapping("/unit/{unitId}")
    public Result<List<WfInstance>> getByUnit(@PathVariable Long unitId) {
        return Result.success(instanceService.getByUnitId(unitId));
    }

    @Audit(action = "取消流程实例")
    @Operation(summary = "取消实例")
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancel(@PathVariable Long id) {
        return Result.success(instanceService.cancelInstance(id));
    }
}