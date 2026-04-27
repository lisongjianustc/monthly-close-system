package com.mc.rule.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体 (rule模块专用)
 */
@Data
public class RuleBaseEntity implements Serializable {

    private Long id;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
