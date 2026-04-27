package com.mc.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.system.entity.SysPeriod;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会计期间Mapper
 */
@Mapper
public interface SysPeriodMapper extends BaseMapper<SysPeriod> {

    /**
     * 查询当年所有期间
     */
    List<SysPeriod> selectByYear(@Param("year") Integer year);

    /**
     * 查询本期
     */
    SysPeriod selectCurrent();

    /**
     * 根据编码查询
     */
    SysPeriod selectByCode(@Param("code") String code);
}