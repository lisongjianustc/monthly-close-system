package com.mc.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.enums.WorkflowTemplateStatus;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.workflow.dto.NodeConfig;
import com.mc.workflow.dto.TemplateNodes;
import com.mc.workflow.entity.WfTemplate;
import com.mc.workflow.mapper.WfTemplateMapper;
import com.mc.workflow.service.IWfTemplateService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 流程模板Service实现
 */
@Service
public class WfTemplateServiceImpl extends ServiceImpl<WfTemplateMapper, WfTemplate> implements IWfTemplateService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean createTemplate(WfTemplate template) {
        template.setStatus(WorkflowTemplateStatus.DRAFT.name());
        template.setVersion(1);
        template.setRefCount(0);
        return this.save(template);
    }

    @Override
    public boolean updateTemplate(WfTemplate template) {
        WfTemplate existing = this.getById(template.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "模板不存在");
        }
        if (WorkflowTemplateStatus.PUBLISHED.name().equals(existing.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "已发布的模板不能修改");
        }
        return this.updateById(template);
    }

    @Override
    public boolean publishTemplate(Long id, String releaseNote) {
        WfTemplate template = this.getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "模板不存在");
        }
        if (WorkflowTemplateStatus.PUBLISHED.name().equals(template.getStatus())) {
            // 已发布模板再次发布时创建新版本
            template.setVersion(template.getVersion() + 1);
        }
        // 校验节点配置
        validateNodes(template);
        template.setStatus(WorkflowTemplateStatus.PUBLISHED.name());
        template.setReleaseNote(releaseNote);
        return this.updateById(template);
    }

    @Override
    public boolean archiveTemplate(Long id) {
        WfTemplate template = this.getById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "模板不存在");
        }
        if (template.getRefCount() != null && template.getRefCount() > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "模板已被使用，不能归档");
        }
        template.setStatus(WorkflowTemplateStatus.DEPRECATED.name());
        return this.updateById(template);
    }

    @Override
    public List<WfTemplate> listTemplates(String status) {
        LambdaQueryWrapper<WfTemplate> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WfTemplate::getStatus, status);
        }
        wrapper.orderByDesc(WfTemplate::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public WfTemplate getTemplateDetail(Long id) {
        return this.getById(id);
    }

    @Override
    public TemplateNodes parseNodes(WfTemplate template) {
        if (template.getNodes() == null || template.getNodes().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(template.getNodes(), TemplateNodes.class);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "模板节点配置格式错误: " + e.getMessage());
        }
    }

    @Override
    public void validateNodes(WfTemplate template) {
        TemplateNodes nodes = parseNodes(template);
        if (nodes == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "模板节点配置不能为空");
        }
        if (nodes.getNodes() == null || nodes.getNodes().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "模板至少需要一个节点");
        }
        if (nodes.getStartNodeCode() == null || nodes.getStartNodeCode().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "起始节点编码不能为空");
        }

        Set<String> nodeCodes = new HashSet<>();
        Set<String> startNodeCodes = new HashSet<>();

        for (NodeConfig node : nodes.getNodes()) {
            if (node.getCode() == null || node.getCode().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "节点编码不能为空");
            }
            if (!nodeCodes.add(node.getCode())) {
                throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "节点编码重复: " + node.getCode());
            }
            if (nodes.getStartNodeCode().equals(node.getCode())) {
                startNodeCodes.add(node.getCode());
            }
            if (node.getType() == null || node.getType().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "节点类型不能为空: " + node.getCode());
            }
        }

        if (startNodeCodes.isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "起始节点不存在: " + nodes.getStartNodeCode());
        }

        // 校验前置节点引用
        for (NodeConfig node : nodes.getNodes()) {
            if (node.getPreNodeCodes() != null && !node.getPreNodeCodes().isEmpty()) {
                String[] preCodes = node.getPreNodeCodes().split(",");
                for (String preCode : preCodes) {
                    preCode = preCode.trim();
                    if (!nodeCodes.contains(preCode)) {
                        throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(),
                            "节点 " + node.getCode() + " 的前置节点不存在: " + preCode);
                    }
                }
            }
        }
    }
}