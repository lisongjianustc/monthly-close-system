package com.mc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.system.entity.SysRole;

import java.util.List;

/**
 * 角色Service
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 根据编码查询
     */
    SysRole getByCode(String code);

    /**
     * 新增角色
     */
    boolean addRole(SysRole role);

    /**
     * 修改角色
     */
    boolean updateRole(SysRole role);

    /**
     * 删除角色
     */
    boolean deleteRole(Long id);

    /**
     * 查询用户角色
     */
    List<SysRole> getUserRoles(Long userId);
}