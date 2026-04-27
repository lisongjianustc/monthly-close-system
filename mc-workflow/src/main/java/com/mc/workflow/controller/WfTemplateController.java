package com.mc.workflow.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.workflow.entity.WfTemplate;
import com.mc.workflow.service.IWfTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程模板Controller
 */
@Tag(name = "流程模板管理")
@RestController
@RequestMapping("/workflow/template")
public class WfTemplateController {

    @Autowired
    private IWfTemplateService templateService;

    @Operation(summary = "查询模板列表")
    @GetMapping("/list")
    public Result<List<WfTemplate>> list(@RequestParam(required = false) String status) {
        return Result.success(templateService.listTemplates(status));
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<WfTemplate> getDetail(@PathVariable Long id) {
        return Result.success(templateService.getTemplateDetail(id));
    }

    @Audit(action = "创建流程模板")
    @Operation(summary = "创建模板")
    @PostMapping
    public Result<Boolean> create(@RequestBody WfTemplate template) {
        return Result.success(templateService.createTemplate(template));
    }

    @Audit(action = "更新流程模板")
    @Operation(summary = "更新模板")
    @PutMapping
    public Result<Boolean> update(@RequestBody WfTemplate template) {
        return Result.success(templateService.updateTemplate(template));
    }

    @Audit(action = "发布流程模板")
    @Operation(summary = "发布模板")
    @PostMapping("/publish/{id}")
    public Result<Boolean> publish(@PathVariable Long id, @RequestParam(required = false) String releaseNote) {
        return Result.success(templateService.publishTemplate(id, releaseNote));
    }

    @Audit(action = "归档流程模板")
    @Operation(summary = "归档模板")
    @PostMapping("/archive/{id}")
    public Result<Boolean> archive(@PathVariable Long id) {
        return Result.success(templateService.archiveTemplate(id));
    }
}