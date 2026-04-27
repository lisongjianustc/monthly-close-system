package com.mc.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.common.entity.AuditLog;

import java.util.List;

/**
 * 审计日志服务接口
 */
public interface IAuditLogService extends IService<AuditLog> {

    /**
     * 保存审计日志
     */
    void saveAuditLog(AuditLog auditLog);

    /**
     * 根据操作人查询
     */
    List<AuditLog> listByOperatorId(Long operatorId);

    /**
     * 根据业务ID查询
     */
    List<AuditLog> listByBizId(Long bizId);
}
