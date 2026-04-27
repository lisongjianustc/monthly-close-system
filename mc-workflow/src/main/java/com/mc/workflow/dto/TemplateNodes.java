package com.mc.workflow.dto;

import lombok.Data;

import java.util.List;

/**
 * 模板节点配置DTO
 */
@Data
public class TemplateNodes {

    /** 起始节点编码 */
    private String startNodeCode;

    /** 节点列表 */
    private List<NodeConfig> nodes;
}
