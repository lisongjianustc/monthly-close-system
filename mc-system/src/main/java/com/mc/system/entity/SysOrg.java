package com.mc.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 组织树节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends BaseEntity {

    /** 父节点ID */
    private Long parentId;

    /** 节点编码 */
    @JsonProperty("orgCode")
    private String code;

    /** 节点名称 */
    @JsonProperty("orgName")
    private String name;

    /** 节点类型: UNIT-单位, DEPT-部门 */
    private String type;

    /** 层级路径 */
    private String path;

    /** 层级深度 */
    private Integer level;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 子节点 (非数据库字段) */
    @TableField(exist = false)
    private List<SysOrg> children;
}