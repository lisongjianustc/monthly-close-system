package com.mc.system.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.PageResult;
import com.mc.common.result.Result;
import com.mc.system.entity.SysUser;
import com.mc.system.service.ISysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户Controller
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "分页查询")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long orgId) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (orgId != null) {
            wrapper.eq(SysUser::getOrgId, orgId);
        }
        Page<SysUser> result = sysUserService.page(page, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getSize(), result.getCurrent(), result.getRecords()));
    }

    @Operation(summary = "获取用户")
    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        user.setPassword(null);
        return Result.success(user);
    }

    @Audit(action = "新增用户")
    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Boolean> add(@RequestBody SysUser user) {
        return Result.success(sysUserService.addUser(user));
    }

    @Audit(action = "修改用户")
    @Operation(summary = "修改用户")
    @PutMapping
    public Result<Boolean> update(@RequestBody SysUser user) {
        return Result.success(sysUserService.updateUser(user));
    }

    @Audit(action = "删除用户")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysUserService.deleteUser(id));
    }

    @Audit(action = "重置密码")
    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword/{id}")
    public Result<Boolean> resetPassword(@PathVariable Long id,
                                          @RequestParam String oldPassword,
                                          @RequestParam String newPassword) {
        return Result.success(sysUserService.resetPassword(id, oldPassword, newPassword));
    }

    @Audit(action = "修改密码")
    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result<Boolean> changePassword(@RequestParam Long id,
                                          @RequestParam String oldPassword,
                                          @RequestParam String newPassword) {
        return Result.success(sysUserService.changePassword(id, oldPassword, newPassword));
    }
}