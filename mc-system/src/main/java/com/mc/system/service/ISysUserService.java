package com.mc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.system.entity.SysUser;

import java.util.List;

/**
 * 用户Service
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询
     */
    SysUser getByUsername(String username);

    /**
     * 新增用户
     */
    boolean addUser(SysUser user);

    /**
     * 修改用户
     */
    boolean updateUser(SysUser user);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);

    /**
     * 重置密码
     */
    boolean resetPassword(Long id, String oldPassword, String newPassword);

    /**
     * 修改密码
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);
}