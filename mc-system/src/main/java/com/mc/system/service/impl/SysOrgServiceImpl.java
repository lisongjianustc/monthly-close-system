package com.mc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.system.entity.SysOrg;
import com.mc.system.mapper.SysOrgMapper;
import com.mc.system.service.ISysOrgService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织树Service实现
 */
@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements ISysOrgService {

    @Override
    public List<SysOrg> getTree() {
        List<SysOrg> all = this.list();
        return buildTree(all, null);
    }

    @Override
    public List<SysOrg> getChildren(Long parentId) {
        return this.list(new LambdaQueryWrapper<SysOrg>()
                .eq(SysOrg::getParentId, parentId)
                .orderByAsc(SysOrg::getSort));
    }

    @Override
    public boolean addOrg(SysOrg org) {
        // 检查编码唯一性
        if (this.count(new LambdaQueryWrapper<SysOrg>().eq(SysOrg::getCode, org.getCode())) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "编码已存在");
        }
        // 设置层级路径
        if (org.getParentId() != null && org.getParentId() > 0) {
            SysOrg parent = this.getById(org.getParentId());
            if (parent != null) {
                org.setPath(parent.getPath() + "/" + org.getCode());
                org.setLevel(parent.getLevel() + 1);
            }
        } else {
            org.setPath("/" + org.getCode());
            org.setLevel(1);
        }
        return this.save(org);
    }

    @Override
    public boolean updateOrg(SysOrg org) {
        if (!StringUtils.hasText(org.getCode())) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "编码不能为空");
        }
        return this.updateById(org);
    }

    @Override
    public boolean deleteOrg(Long id) {
        // 检查是否有子节点
        if (this.count(new LambdaQueryWrapper<SysOrg>().eq(SysOrg::getParentId, id)) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "存在子节点，无法删除");
        }
        return this.removeById(id);
    }

    private List<SysOrg> buildTree(List<SysOrg> all, Long parentId) {
        return all.stream()
                .filter(org -> (parentId == null && org.getParentId() == null) ||
                        (parentId != null && parentId.equals(org.getParentId())))
                .peek(org -> org.setChildren(buildTree(all, org.getId())))
                .collect(Collectors.toList());
    }
}