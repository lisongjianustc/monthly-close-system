package com.mc.system.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.system.entity.SysOrg;
import com.mc.system.service.ISysOrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织树Controller
 */
@Tag(name = "组织树管理")
@RestController
@RequestMapping("/system/org")
public class SysOrgController {

    @Autowired
    private ISysOrgService sysOrgService;

    @Operation(summary = "获取组织树")
    @GetMapping("/tree")
    public Result<List<SysOrg>> getTree() {
        return Result.success(sysOrgService.getTree());
    }

    @Operation(summary = "获取子节点")
    @GetMapping("/children/{parentId}")
    public Result<List<SysOrg>> getChildren(@PathVariable Long parentId) {
        return Result.success(sysOrgService.getChildren(parentId));
    }

    @Audit(action = "新增组织")
    @Operation(summary = "新增组织")
    @PostMapping
    public Result<Boolean> add(@RequestBody SysOrg org) {
        return Result.success(sysOrgService.addOrg(org));
    }

    @Audit(action = "修改组织")
    @Operation(summary = "修改组织")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysOrg org) {
        return Result.success(sysOrgService.updateOrg(org));
    }

    @Audit(action = "删除组织")
    @Operation(summary = "删除组织")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysOrgService.deleteOrg(id));
    }
}