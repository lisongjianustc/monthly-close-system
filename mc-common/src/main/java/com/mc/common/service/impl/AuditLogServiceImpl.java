package com.mc.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.common.entity.AuditLog;
import com.mc.common.mapper.AuditLogMapper;
import com.mc.common.service.IAuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审计日志服务实现
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements IAuditLogService {

    @Override
    public void saveAuditLog(AuditLog auditLog) {
        this.save(auditLog);
    }

    @Override
    public List<AuditLog> listByOperatorId(Long operatorId) {
        return this.list(new LambdaQueryWrapper<AuditLog>()
                .eq(AuditLog::getOperatorId, operatorId)
                .orderByDesc(AuditLog::getCreateTime));
    }

    @Override
    public List<AuditLog> listByBizId(Long bizId) {
        return this.list(new LambdaQueryWrapper<AuditLog>()
                .eq(AuditLog::getBizId, bizId)
                .orderByDesc(AuditLog::getCreateTime));
    }
}
