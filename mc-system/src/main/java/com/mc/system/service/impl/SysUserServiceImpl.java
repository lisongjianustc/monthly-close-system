package com.mc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.system.entity.SysUser;
import com.mc.system.mapper.SysUserMapper;
import com.mc.system.service.ISysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户Service实现
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public SysUser getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    public boolean addUser(SysUser user) {
        // 检查用户名唯一性
        if (this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return this.save(user);
    }

    @Override
    public boolean updateUser(SysUser user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return this.updateById(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean resetPassword(Long id, String oldPassword, String newPassword) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        // 验证当前操作者身份（必须提供正确原密码）
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }
}