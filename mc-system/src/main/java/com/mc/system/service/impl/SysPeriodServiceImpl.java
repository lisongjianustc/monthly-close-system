package com.mc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.common.enums.PeriodStatus;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.system.entity.SysPeriod;
import com.mc.system.mapper.SysPeriodMapper;
import com.mc.system.service.ISysPeriodService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会计期间Service实现
 */
@Service
public class SysPeriodServiceImpl extends ServiceImpl<SysPeriodMapper, SysPeriod> implements ISysPeriodService {

    @Override
    public List<SysPeriod> getByYear(Integer year) {
        return this.list(new LambdaQueryWrapper<SysPeriod>()
                .eq(SysPeriod::getYear, year)
                .orderByAsc(SysPeriod::getMonth));
    }

    @Override
    public SysPeriod getCurrent() {
        return this.getOne(new LambdaQueryWrapper<SysPeriod>()
                .eq(SysPeriod::getIsCurrent, 1));
    }

    @Override
    public boolean addPeriod(SysPeriod period) {
        // 检查编码唯一性
        if (this.count(new LambdaQueryWrapper<SysPeriod>().eq(SysPeriod::getCode, period.getCode())) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "期间编码已存在");
        }
        // 解析年月
        if (period.getCode() != null && period.getCode().length() >= 7) {
            period.setYear(Integer.parseInt(period.getCode().substring(0, 4)));
            period.setMonth(Integer.parseInt(period.getCode().substring(5, 7)));
        }
        return this.save(period);
    }

    @Override
    public boolean updatePeriod(SysPeriod period) {
        // 不能修改已关闭期间的基本信息
        SysPeriod existing = this.getById(period.getId());
        if (existing != null && PeriodStatus.CLOSED.name().equals(existing.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "已关闭的期间不能修改");
        }
        return this.updateById(period);
    }

    @Override
    public boolean closePeriod(Long id) {
        SysPeriod period = this.getById(id);
        if (period == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "期间不存在");
        }
        if (PeriodStatus.ACTIVE.name().equals(period.getStatus())) {
            period.setStatus(PeriodStatus.CLOSED.name());
        }
        return this.updateById(period);
    }
}