package com.mc.data.dto;

import lombok.Data;

/**
 * 导入进度DTO
 */
@Data
public class ImportProgress {

    /** 批次ID */
    private Long batchId;

    /** 批次号 */
    private String batchNo;

    /** 状态 */
    private String status;

    /** 总记录数 */
    private Integer totalRecords;

    /** 已处理数 */
    private Integer processedRecords;

    /** 成功数 */
    private Integer successRecords;

    /** 失败数 */
    private Integer failRecords;

    /** 进度百分比 */
    private Integer progressPercent;

    /** 当前处理行 */
    private Integer currentRow;

    /** 开始时间 */
    private String startTime;

    /** 预计剩余时间(秒) */
    private Integer estimatedRemainingSeconds;
}
