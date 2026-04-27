package com.mc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.system.entity.SysOrg;

import java.util.List;

/**
 * 组织树Service
 */
public interface ISysOrgService extends IService<SysOrg> {

    /**
     * 查询树形结构
     */
    List<SysOrg> getTree();

    /**
     * 查询子节点
     */
    List<SysOrg> getChildren(Long parentId);

    /**
     * 新增节点
     */
    boolean addOrg(SysOrg org);

    /**
     * 修改节点
     */
    boolean updateOrg(SysOrg org);

    /**
     * 删除节点
     */
    boolean deleteOrg(Long id);
}