package com.mc.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会计期间
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_period")
public class SysPeriod extends BaseEntity {

    /** 期间编码 (如: 2026-04) */
    @JsonProperty("periodCode")
    private String code;

    /** 期间名称 (如: 2026年4月) */
    @JsonProperty("periodName")
    private String name;

    /** 起始日期 */
    private String startDate;

    /** 结束日期 */
    private String endDate;

    /** 所属年度 */
    private Integer year;

    /** 所属月度 */
    private Integer month;

    /** 状态: DRAFT-草稿, OPEN-开放, CLOSED-关闭, ARCHIVED-归档 */
    private String status;

    /** 是否本期: 0-否, 1-是 */
    private Integer isCurrent;
}