package com.mc.system.controller;

import com.mc.common.result.Result;
import com.mc.common.util.JwtUtil;
import com.mc.system.entity.SysUser;
import com.mc.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录Controller
 */
@Tag(name = "登录管理")
@RestController
@RequestMapping("/system/auth")
public class AuthController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return Result.fail(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.fail(401, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            return Result.fail(403, "用户已禁用");
        }
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        return Result.success(data);
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/current")
    public Result<Map<String, Object>> current(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未登录");
        }
        String token = authHeader.substring(7);
        Map<String, Object> claims = JwtUtil.parseToken(token);
        if (claims == null) {
            return Result.fail(401, "Token无效或已过期");
        }
        Long userId = (Long) claims.get("userId");
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("orgId", user.getOrgId());
        return Result.success(data);
    }

    @Operation(summary = "退出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null);
    }
}
