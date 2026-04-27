package com.mc.exception.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.exception.entity.BusinessExceptionRecord;

import java.util.List;
import java.util.Map;

/**
 * 异常处理Service
 */
public interface IExceptionService extends IService<BusinessExceptionRecord> {

    /**
     * 记录异常
     */
    Long recordException(String exceptionCode, String exceptionType, String severity,
                        String module, String businessId, Long periodId, Long unitId,
                        String message, String stackTrace);

    /**
     * 查询待处理异常
     */
    List<BusinessExceptionRecord> listPendingExceptions(Long periodId, Long unitId);

    /**
     * 查询异常详情
     */
    BusinessExceptionRecord getExceptionDetail(Long id);

    /**
     * 处理异常
     */
    boolean handleException(Long id, Long handlerId, String handlerName, String comment);

    /**
     * 忽略异常
     */
    boolean ignoreException(Long id, String reason);

    /**
     * 统计异常
     */
    Map<String, Long> statisticsException(Long periodId, Long unitId);

    /**
     * 批量处理
     */
    boolean batchHandle(List<Long> ids, Long handlerId, String handlerName);
}
