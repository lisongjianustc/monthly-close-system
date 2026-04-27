package com.mc.system.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.system.entity.SysRole;
import com.mc.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色Controller
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<?> page() {
        return Result.success(sysRoleService.list());
    }

    @Operation(summary = "获取角色")
    @GetMapping("/{id}")
    public Result<SysRole> get(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }

    @Audit(action = "新增角色")
    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Boolean> add(@RequestBody SysRole role) {
        return Result.success(sysRoleService.addRole(role));
    }

    @Audit(action = "修改角色")
    @Operation(summary = "修改角色")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysRole role) {
        return Result.success(sysRoleService.updateRole(role));
    }

    @Audit(action = "删除角色")
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysRoleService.deleteRole(id));
    }
}