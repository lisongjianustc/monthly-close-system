package com.mc.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.workflow.entity.WfInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程实例Mapper
 */
@Mapper
public interface WfInstanceMapper extends BaseMapper<WfInstance> {

    /**
     * 查询期间实例
     */
    List<WfInstance> selectByPeriodId(@Param("periodId") Long periodId);

    /**
     * 查询单位实例
     */
    List<WfInstance> selectByUnitId(@Param("unitId") Long unitId);
}