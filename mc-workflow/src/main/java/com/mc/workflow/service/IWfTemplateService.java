package com.mc.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mc.workflow.dto.TemplateNodes;
import com.mc.workflow.entity.WfTemplate;

import java.util.List;

/**
 * 流程模板Service
 */
public interface IWfTemplateService extends IService<WfTemplate> {

    /**
     * 创建模板(草稿)
     */
    boolean createTemplate(WfTemplate template);

    /**
     * 更新模板
     */
    boolean updateTemplate(WfTemplate template);

    /**
     * 发布模板
     */
    boolean publishTemplate(Long id, String releaseNote);

    /**
     * 归档模板
     */
    boolean archiveTemplate(Long id);

    /**
     * 查询模板列表
     */
    List<WfTemplate> listTemplates(String status);

    /**
     * 获取模板详情
     */
    WfTemplate getTemplateDetail(Long id);

    /**
     * 解析模板节点配置
     */
    TemplateNodes parseNodes(WfTemplate template);

    /**
     * 校验模板节点配置
     */
    void validateNodes(WfTemplate template);
}