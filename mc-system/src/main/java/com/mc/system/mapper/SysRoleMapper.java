package com.mc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据编码查询
     */
    SysRole selectByCode(@Param("code") String code);
}