package com.mc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询树形结构
     */
    List<SysMenu> selectTree();

    /**
     * 查询用户菜单
     */
    List<SysMenu> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询角色菜单
     */
    List<SysMenu> selectByRoleId(@Param("roleId") Long roleId);
}