package com.mc.exception.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.exception.entity.BusinessExceptionRecord;
import com.mc.exception.mapper.BusinessExceptionRecordMapper;
import com.mc.exception.service.IExceptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 异常处理Service实现
 */
@Service
public class ExceptionServiceImpl extends ServiceImpl<BusinessExceptionRecordMapper, BusinessExceptionRecord> implements IExceptionService {

    @Override
    @Transactional
    public Long recordException(String exceptionCode, String exceptionType, String severity,
                                 String module, String businessId, Long periodId, Long unitId,
                                 String message, String stackTrace) {
        BusinessExceptionRecord record = new BusinessExceptionRecord();
        record.setExceptionCode(exceptionCode);
        record.setExceptionType(exceptionType);
        record.setSeverity(severity);
        record.setModule(module);
        record.setBusinessId(businessId);
        record.setPeriodId(periodId);
        record.setUnitId(unitId);
        record.setMessage(message);
        record.setStackTrace(stackTrace);
        record.setStatus("PENDING");
        this.save(record);
        return record.getId();
    }

    @Override
    public List<BusinessExceptionRecord> listPendingExceptions(Long periodId, Long unitId) {
        LambdaQueryWrapper<BusinessExceptionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessExceptionRecord::getStatus, "PENDING");
        if (periodId != null) {
            wrapper.eq(BusinessExceptionRecord::getPeriodId, periodId);
        }
        if (unitId != null) {
            wrapper.eq(BusinessExceptionRecord::getUnitId, unitId);
        }
        wrapper.orderByDesc(BusinessExceptionRecord::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public BusinessExceptionRecord getExceptionDetail(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional
    public boolean handleException(Long id, Long handlerId, String handlerName, String comment) {
        BusinessExceptionRecord record = this.getById(id);
        if (record == null) {
            return false;
        }
        record.setStatus("RESOLVED");
        record.setHandlerId(handlerId);
        record.setHandlerName(handlerName);
        record.setHandleComment(comment);
        record.setHandleTime(LocalDateTime.now());
        record.setResolveTime(LocalDateTime.now());
        return this.updateById(record);
    }

    @Override
    @Transactional
    public boolean ignoreException(Long id, String reason) {
        BusinessExceptionRecord record = this.getById(id);
        if (record == null) {
            return false;
        }
        record.setStatus("IGNORED");
        record.setHandleComment(reason);
        record.setHandleTime(LocalDateTime.now());
        return this.updateById(record);
    }

    @Override
    public Map<String, Long> statisticsException(Long periodId, Long unitId) {
        Map<String, Long> stats = new HashMap<>();

        // TOTAL
        LambdaQueryWrapper<BusinessExceptionRecord> wrapper = new LambdaQueryWrapper<>();
        if (periodId != null) {
            wrapper.eq(BusinessExceptionRecord::getPeriodId, periodId);
        }
        if (unitId != null) {
            wrapper.eq(BusinessExceptionRecord::getUnitId, unitId);
        }
        stats.put("TOTAL", this.count(wrapper));

        // PENDING
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessExceptionRecord::getStatus, "PENDING");
        if (periodId != null) {
            wrapper.eq(BusinessExceptionRecord::getPeriodId, periodId);
        }
        if (unitId != null) {
            wrapper.eq(BusinessExceptionRecord::getUnitId, unitId);
        }
        stats.put("PENDING", this.count(wrapper));

        // RESOLVED
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessExceptionRecord::getStatus, "RESOLVED");
        if (periodId != null) {
            wrapper.eq(BusinessExceptionRecord::getPeriodId, periodId);
        }
        if (unitId != null) {
            wrapper.eq(BusinessExceptionRecord::getUnitId, unitId);
        }
        stats.put("RESOLVED", this.count(wrapper));

        // IGNORED
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessExceptionRecord::getStatus, "IGNORED");
        if (periodId != null) {
            wrapper.eq(BusinessExceptionRecord::getPeriodId, periodId);
        }
        if (unitId != null) {
            wrapper.eq(BusinessExceptionRecord::getUnitId, unitId);
        }
        stats.put("IGNORED", this.count(wrapper));

        return stats;
    }

    @Override
    @Transactional
    public boolean batchHandle(List<Long> ids, Long handlerId, String handlerName) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        List<BusinessExceptionRecord> toUpdate = this.listByIds(ids).stream()
            .filter(r -> "PENDING".equals(r.getStatus()))
            .peek(r -> {
                r.setStatus("RESOLVED");
                r.setHandlerId(handlerId);
                r.setHandlerName(handlerName);
                r.setHandleTime(now);
                r.setResolveTime(now);
            })
            .toList();
        if (!toUpdate.isEmpty()) {
            this.updateBatchById(toUpdate);
        }
        return true;
    }
}
