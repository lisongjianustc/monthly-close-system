package com.mc.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.system.entity.SysPeriod;

import java.util.List;

/**
 * 会计期间Service
 */
public interface ISysPeriodService extends IService<SysPeriod> {

    /**
     * 查询当年所有期间
     */
    List<SysPeriod> getByYear(Integer year);

    /**
     * 查询本期
     */
    SysPeriod getCurrent();

    /**
     * 新增期间
     */
    boolean addPeriod(SysPeriod period);

    /**
     * 修改期间
     */
    boolean updatePeriod(SysPeriod period);

    /**
     * 关闭期间
     */
    boolean closePeriod(Long id);
}