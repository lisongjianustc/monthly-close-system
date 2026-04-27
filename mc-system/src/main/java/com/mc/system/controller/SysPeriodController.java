package com.mc.system.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.system.entity.SysPeriod;
import com.mc.system.service.ISysPeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会计期间Controller
 */
@Tag(name = "会计期间管理")
@RestController
@RequestMapping("/system/period")
public class SysPeriodController {

    @Autowired
    private ISysPeriodService sysPeriodService;

    @Operation(summary = "获取本期")
    @GetMapping("/current")
    public Result<SysPeriod> getCurrent() {
        return Result.success(sysPeriodService.getCurrent());
    }

    @Operation(summary = "列表查询")
    @GetMapping("/list")
    public Result<List<SysPeriod>> list() {
        return Result.success(sysPeriodService.list());
    }

    @Operation(summary = "按年度查询")
    @GetMapping("/year/{year}")
    public Result<List<SysPeriod>> getByYear(@PathVariable Integer year) {
        return Result.success(sysPeriodService.getByYear(year));
    }

    @Audit(action = "新增期间")
    @Operation(summary = "新增期间")
    @PostMapping
    public Result<Boolean> add(@RequestBody SysPeriod period) {
        return Result.success(sysPeriodService.addPeriod(period));
    }

    @Audit(action = "修改期间")
    @Operation(summary = "修改期间")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysPeriod period) {
        return Result.success(sysPeriodService.updatePeriod(period));
    }

    @Audit(action = "关闭期间")
    @Operation(summary = "关闭期间")
    @PostMapping("/close/{id}")
    public Result<Boolean> close(@PathVariable Long id) {
        return Result.success(sysPeriodService.closePeriod(id));
    }
}