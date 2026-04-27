package com.mc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单Service
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单树
     */
    List<SysMenu> getTree();

    /**
     * 查询用户菜单
     */
    List<SysMenu> getUserMenus(Long userId);

    /**
     * 新增菜单
     */
    boolean addMenu(SysMenu menu);

    /**
     * 修改菜单
     */
    boolean updateMenu(SysMenu menu);

    /**
     * 删除菜单
     */
    boolean deleteMenu(Long id);
}