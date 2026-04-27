package com.mc.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.workflow.dto.NodeConfig;
import com.mc.workflow.dto.TemplateNodes;
import com.mc.workflow.entity.WfInstance;
import com.mc.workflow.entity.WfTask;
import com.mc.workflow.entity.WfTemplate;
import com.mc.workflow.mapper.WfInstanceMapper;
import com.mc.workflow.service.IWfInstanceService;
import com.mc.workflow.service.IWfTaskService;
import com.mc.workflow.service.IWfTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 流程实例Service实现
 */
@Service
public class WfInstanceServiceImpl extends ServiceImpl<WfInstanceMapper, WfInstance> implements IWfInstanceService {

    @Autowired
    private IWfTemplateService templateService;

    @Autowired
    private IWfTaskService taskService;

    @Override
    @Transactional
    public Long instantiate(Long templateId, Long periodId, Long unitId) {
        WfInstance instance = new WfInstance();
        instance.setTemplateId(templateId);
        instance.setPeriodId(periodId);
        instance.setUnitId(unitId);
        instance.setStatus("ACTIVE");
        instance.setStartTime(LocalDateTime.now());
        this.save(instance);

        // 派发初始任务
        dispatchTasks(instance.getId());

        return instance.getId();
    }

    @Override
    public List<WfInstance> getByPeriodId(Long periodId) {
        return this.baseMapper.selectByPeriodId(periodId);
    }

    @Override
    public List<WfInstance> getByUnitId(Long unitId) {
        return this.baseMapper.selectByUnitId(unitId);
    }

    @Override
    public boolean cancelInstance(Long id) {
        WfInstance instance = this.getById(id);
        if (instance != null) {
            instance.setStatus("CANCELLED");
            instance.setEndTime(LocalDateTime.now());
            return this.updateById(instance);
        }
        return false;
    }

    @Override
    @Transactional
    public List<WfTask> dispatchTasks(Long instanceId) {
        WfInstance instance = this.getById(instanceId);
        if (instance == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "流程实例不存在");
        }

        WfTemplate template = templateService.getById(instance.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "流程模板不存在");
        }

        TemplateNodes templateNodes = templateService.parseNodes(template);
        if (templateNodes == null || templateNodes.getNodes() == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "模板节点配置为空");
        }

        // 按拓扑排序创建任务
        List<NodeConfig> sortedNodes = topologicalSort(templateNodes);
        Map<String, Long> nodeCodeToTaskId = new HashMap<>();
        List<WfTask> createdTasks = new ArrayList<>();

        for (NodeConfig nodeConfig : sortedNodes) {
            if (Boolean.TRUE.equals(nodeConfig.getDisabled())) {
                continue;
            }

            // 构建前置任务ID字符串
            String preTaskIds = buildPreTaskIds(nodeConfig, nodeCodeToTaskId);

            // 计算SLA截止时间
            LocalDateTime slaDeadline = calculateSlaDeadline(nodeConfig);

            Long taskId = taskService.createTask(
                instanceId,
                nodeConfig.getCode(),
                nodeConfig.getName(),
                nodeConfig.getType(),
                nodeConfig.getAssigneeId(),
                nodeConfig.getAssigneeName(),
                nodeConfig.getSlaHours() != null ? nodeConfig.getSlaHours() : 24
            );

            nodeCodeToTaskId.put(nodeConfig.getCode(), taskId);

            WfTask task = taskService.getById(taskId);
            if (task != null && preTaskIds != null && !preTaskIds.isEmpty()) {
                task.setPreTaskIds(preTaskIds);
                task.setSlaDeadline(slaDeadline);
                taskService.updateById(task);
            }
            createdTasks.add(task);
        }

        // 更新模板引用次数
        template.setRefCount(template.getRefCount() == null ? 1 : template.getRefCount() + 1);
        templateService.updateById(template);

        return createdTasks;
    }

    private List<NodeConfig> topologicalSort(TemplateNodes templateNodes) {
        Map<String, NodeConfig> nodeMap = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        List<NodeConfig> allNodes = templateNodes.getNodes();

        for (NodeConfig node : allNodes) {
            nodeMap.put(node.getCode(), node);
            inDegree.put(node.getCode(), 0);
        }

        // 计算入度
        for (NodeConfig node : allNodes) {
            if (node.getPreNodeCodes() != null && !node.getPreNodeCodes().isEmpty()) {
                String[] preCodes = node.getPreNodeCodes().split(",");
                for (String preCode : preCodes) {
                    inDegree.put(node.getCode(), inDegree.getOrDefault(node.getCode(), 0) + 1);
                }
            }
        }

        // 从起始节点开始BFS
        Queue<String> queue = new LinkedList<>();
        for (NodeConfig node : allNodes) {
            if (templateNodes.getStartNodeCode().equals(node.getCode())) {
                queue.offer(node.getCode());
            }
        }

        List<NodeConfig> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String code = queue.poll();
            NodeConfig node = nodeMap.get(code);
            if (node != null) {
                sorted.add(node);
            }

            // 找到所有以当前节点为前置的节点
            for (NodeConfig n : allNodes) {
                if (n.getPreNodeCodes() != null && n.getPreNodeCodes().contains(code)) {
                    int degree = inDegree.get(n.getCode()) - 1;
                    inDegree.put(n.getCode(), degree);
                    if (degree == 0) {
                        queue.offer(n.getCode());
                    }
                }
            }
        }

        return sorted;
    }

    private String buildPreTaskIds(NodeConfig nodeConfig, Map<String, Long> nodeCodeToTaskId) {
        if (nodeConfig.getPreNodeCodes() == null || nodeConfig.getPreNodeCodes().isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String[] preCodes = nodeConfig.getPreNodeCodes().split(",");
        for (int i = 0; i < preCodes.length; i++) {
            String preCode = preCodes[i].trim();
            Long preTaskId = nodeCodeToTaskId.get(preCode);
            if (preTaskId != null) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(preTaskId);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private LocalDateTime calculateSlaDeadline(NodeConfig nodeConfig) {
        if (nodeConfig.getSlaHours() == null || nodeConfig.getSlaHours() <= 0) {
            return LocalDateTime.now().plusHours(24); // 默认24小时
        }
        return LocalDateTime.now().plusHours(nodeConfig.getSlaHours());
    }
}