package com.mc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.system.entity.SysRole;
import com.mc.system.mapper.SysRoleMapper;
import com.mc.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色Service实现
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Override
    public SysRole getByCode(String code) {
        return this.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code));
    }

    @Override
    public boolean addRole(SysRole role) {
        if (this.count(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, role.getCode())) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "角色编码已存在");
        }
        return this.save(role);
    }

    @Override
    public boolean updateRole(SysRole role) {
        return this.updateById(role);
    }

    @Override
    public boolean deleteRole(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<SysRole> getUserRoles(Long userId) {
        // TODO: 关联查询用户角色关系表
        return List.of();
    }
}