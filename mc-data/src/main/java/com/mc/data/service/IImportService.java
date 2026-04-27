package com.mc.data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.data.dto.ImportProgress;
import com.mc.data.entity.ImportBatch;
import com.mc.data.entity.ImportRecord;

import java.util.List;

/**
 * 数据导入Service
 */
public interface IImportService extends IService<ImportBatch> {

    /**
     * 创建导入批次
     */
    Long createBatch(String importType, String fileName, String filePath, Long fileSize, Long userId, String userName);

    /**
     * 执行导入
     */
    void executeImport(Long batchId, String templateCode);

    /**
     * 批量插入记录（事务方法，供内部调用）
     */
    void doInsertBatch(Long batchId, List<ImportRecord> records);

    /**
     * 查询导入进度
     */
    ImportProgress getImportProgress(Long batchId);

    /**
     * 查询批次的所有记录
     */
    List<ImportRecord> getBatchRecords(Long batchId);

    /**
     * 查询失败记录
     */
    List<ImportRecord> getFailRecords(Long batchId);

    /**
     * 校验数据
     */
    void validateData(Long batchId);

    /**
     * 取消导入
     */
    boolean cancelImport(Long batchId);
}
