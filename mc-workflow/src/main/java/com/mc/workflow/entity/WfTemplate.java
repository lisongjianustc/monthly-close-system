package com.mc.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_template")
public class WfTemplate extends WorkflowBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模板编码 */
    private String code;

    /** 模板名称 */
    private String name;

    /** 版本号 */
    private Integer version;

    /** 状态: DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已停用 */
    private String status;

    /** 引用次数 */
    private Integer refCount;

    /** 节点配置JSON */
    private String nodes;

    /** 发布说明 */
    private String releaseNote;
}