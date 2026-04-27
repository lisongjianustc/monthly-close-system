package com.mc.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 导入批次
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("import_batch")
public class ImportBatch extends DataBaseEntity {

    /** 批次号 */
    private String batchNo;

    /** 导入类型: EXCEL, CSV, MANUAL */
    private String importType;

    /** 文件名 */
    private String fileName;

    /** 文件路径 */
    private String filePath;

    /** 文件大小 */
    private Long fileSize;

    /** 总记录数 */
    private Integer totalRecords;

    /** 成功数 */
    private Integer successRecords;

    /** 失败数 */
    private Integer failRecords;

    /** 状态: PENDING-待处理, PROCESSING-处理中, COMPLETED-已完成, FAILED-失败 */
    private String status;

    /** 导入用户ID */
    private Long importUserId;

    /** 导入用户名称 */
    private String importUserName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 错误信息 */
    private String errorMsg;
}
