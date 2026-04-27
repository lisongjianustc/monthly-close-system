package com.mc.data.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体 (data模块专用)
 */
@Data
public class DataBaseEntity implements Serializable {

    private Long id;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
