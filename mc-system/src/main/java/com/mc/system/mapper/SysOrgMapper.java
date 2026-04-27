package com.mc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.system.entity.SysOrg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 组织树Mapper
 */
@Mapper
public interface SysOrgMapper extends BaseMapper<SysOrg> {

    /**
     * 查询子节点
     */
    List<SysOrg> selectChildren(@Param("parentId") Long parentId);

    /**
     * 查询树形结构
     */
    List<SysOrg> selectTree();

    /**
     * 根据编码查询
     */
    SysOrg selectByCode(@Param("code") String code);
}