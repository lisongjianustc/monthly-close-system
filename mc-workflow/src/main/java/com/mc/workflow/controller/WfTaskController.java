package com.mc.workflow.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.workflow.entity.WfTask;
import com.mc.workflow.entity.WfTaskTrace;
import com.mc.workflow.service.IWfTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务实例Controller
 */
@Tag(name = "任务实例管理")
@RestController
@RequestMapping("/workflow/task")
public class WfTaskController {

    @Autowired
    private IWfTaskService taskService;

    @Operation(summary = "查询待办任务")
    @GetMapping("/pending/{assigneeId}")
    public Result<List<WfTask>> getPending(@PathVariable Long assigneeId) {
        return Result.success(taskService.getPendingTasks(assigneeId));
    }

    @Operation(summary = "查询任务列表")
    @GetMapping("/list")
    public Result<List<WfTask>> list() {
        return Result.success(taskService.list());
    }

    @Operation(summary = "查询逾期任务")
    @GetMapping("/overdue")
    public Result<List<WfTask>> getOverdue() {
        return Result.success(taskService.getOverdueTasks());
    }

    @Operation(summary = "查询任务轨迹")
    @GetMapping("/trace/{taskId}")
    public Result<List<WfTaskTrace>> getTraces(@PathVariable Long taskId) {
        return Result.success(taskService.getTaskTraces(taskId));
    }

    @Audit(action = "开始任务")
    @Operation(summary = "开始任务")
    @PutMapping("/start/{id}")
    public Result<Boolean> start(@PathVariable Long id) {
        return Result.success(taskService.startTask(id));
    }

    @Audit(action = "提交任务")
    @Operation(summary = "提交任务")
    @PutMapping("/submit/{id}")
    public Result<Boolean> submit(@PathVariable Long id, @RequestParam(required = false) String comment) {
        return Result.success(taskService.submitTask(id, comment));
    }

    @Audit(action = "复核通过")
    @Operation(summary = "复核通过")
    @PutMapping("/approve/{id}")
    public Result<Boolean> approve(@PathVariable Long id, @RequestParam(required = false) String comment) {
        return Result.success(taskService.approveTask(id, comment));
    }

    @Audit(action = "复核退回")
    @Operation(summary = "复核退回")
    @PutMapping("/reject/{id}")
    public Result<Boolean> reject(@PathVariable Long id, @RequestParam(required = false) String comment) {
        return Result.success(taskService.rejectTask(id, comment));
    }
}