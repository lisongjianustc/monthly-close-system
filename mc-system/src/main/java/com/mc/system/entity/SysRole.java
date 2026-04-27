package com.mc.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色编码 */
    @JsonProperty("roleCode")
    private String code;

    /** 角色名称 */
    @JsonProperty("roleName")
    private String name;

    /** 角色类型: SYSTEM-系统角色, BUSINESS-业务角色 */
    private String type;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 排序 */
    private Integer sort;
}