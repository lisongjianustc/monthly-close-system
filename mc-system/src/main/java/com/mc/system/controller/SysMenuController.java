package com.mc.system.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.system.entity.SysMenu;
import com.mc.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单Controller
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Autowired
    private ISysMenuService sysMenuService;

    @Operation(summary = "获取菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenu>> getTree() {
        return Result.success(sysMenuService.getTree());
    }

    @Operation(summary = "获取用户菜单")
    @GetMapping("/user/{userId}")
    public Result<List<SysMenu>> getUserMenus(@PathVariable Long userId) {
        return Result.success(sysMenuService.getUserMenus(userId));
    }

    @Audit(action = "新增菜单")
    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<Boolean> add(@RequestBody SysMenu menu) {
        return Result.success(sysMenuService.addMenu(menu));
    }

    @Audit(action = "修改菜单")
    @Operation(summary = "修改菜单")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysMenu menu) {
        return Result.success(sysMenuService.updateMenu(menu));
    }

    @Audit(action = "删除菜单")
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysMenuService.deleteMenu(id));
    }
}