package com.mc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询
     */
    SysUser selectByUsername(@Param("username") String username);
}