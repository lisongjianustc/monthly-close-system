package com.mc.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 导入记录明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("import_record")
public class ImportRecord extends DataBaseEntity {

    /** 批次ID */
    private Long batchId;

    /** 期间ID */
    private Long periodId;

    /** 单位ID */
    private Long unitId;

    /** 科目编码 */
    private String accountCode;

    /** 科目名称 */
    private String accountName;

    /** 金额 */
    private BigDecimal amount;

    /** 数量 */
    private BigDecimal quantity;

    /** 核算维度1 */
    private String dimension1;

    /** 核算维度2 */
    private String dimension2;

    /** 核算维度3 */
    private String dimension3;

    /** 行号 */
    private Integer rowNo;

    /** 状态: VALID-有效, INVALID-无效, DUPLICATE-重复 */
    private String status;

    /** 错误信息 */
    private String errorMsg;

    /** 校验时间 */
    private LocalDateTime validateTime;
}
