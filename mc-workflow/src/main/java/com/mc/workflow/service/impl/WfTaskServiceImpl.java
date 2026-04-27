package com.mc.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.common.enums.TaskStatus;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.common.util.SecurityUtil;
import com.mc.workflow.entity.WfTask;
import com.mc.workflow.entity.WfTaskTrace;
import com.mc.workflow.mapper.WfTaskMapper;
import com.mc.workflow.mapper.WfTaskTraceMapper;
import com.mc.workflow.service.IWfTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务实例Service实现
 */
@Service
public class WfTaskServiceImpl extends ServiceImpl<WfTaskMapper, WfTask> implements IWfTaskService {

    @Autowired
    private WfTaskTraceMapper taskTraceMapper;

    private void verifyAssignee(WfTask task) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "未登录或认证已过期");
        }
        if (!currentUserId.equals(task.getAssigneeId())) {
            throw new BusinessException(ResultCode.TASK_CANNOT_TRANSFER.getCode(), "无权操作此任务");
        }
    }

    @Override
    @Transactional
    public Long createTask(Long instanceId, String nodeCode, String nodeName, String nodeType, Long assigneeId, String assigneeName, Integer slaHours) {
        WfTask task = new WfTask();
        task.setInstanceId(instanceId);
        task.setNodeCode(nodeCode);
        task.setNodeName(nodeName);
        task.setNodeType(nodeType);
        task.setAssigneeId(assigneeId);
        task.setAssigneeName(assigneeName);
        task.setStatus(TaskStatus.PENDING.name());
        task.setSlaDeadline(LocalDateTime.now().plusHours(slaHours));
        this.save(task);
        recordTrace(task.getId(), "CREATE", assigneeId, assigneeName, "任务创建");
        return task.getId();
    }

    @Override
    @Transactional
    public boolean startTask(Long taskId) {
        WfTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND.getCode(), "任务不存在");
        }
        verifyAssignee(task);
        if (!TaskStatus.PENDING.name().equals(task.getStatus())) {
            throw new BusinessException(ResultCode.TASK_CANNOT_TRANSFER.getCode(), "任务状态不允许开始");
        }
        task.setStatus(TaskStatus.IN_PROGRESS.name());
        task.setStartTime(LocalDateTime.now());
        this.updateById(task);
        recordTrace(taskId, "START", task.getAssigneeId(), task.getAssigneeName(), "任务开始");
        return true;
    }

    @Override
    @Transactional
    public boolean submitTask(Long taskId, String comment) {
        WfTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND.getCode(), "任务不存在");
        }
        verifyAssignee(task);
        if (!TaskStatus.IN_PROGRESS.name().equals(task.getStatus())) {
            throw new BusinessException(ResultCode.TASK_CANNOT_TRANSFER.getCode(), "任务状态不允许提交");
        }
        if (!checkPreTasks(taskId)) {
            throw new BusinessException(ResultCode.TASK_PRECONDITION_NOT_MET.getCode(), "前置任务未完成");
        }
        task.setStatus(TaskStatus.PENDING_REVIEW.name());
        task.setEndTime(LocalDateTime.now());
        this.updateById(task);
        recordTrace(taskId, "SUBMIT", task.getAssigneeId(), task.getAssigneeName(), comment);
        return true;
    }

    @Override
    @Transactional
    public boolean approveTask(Long taskId, String comment) {
        WfTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND.getCode(), "任务不存在");
        }
        // 审批人必须与任务处理人一致
        verifyAssignee(task);
        task.setStatus(TaskStatus.COMPLETED.name());
        task.setEndTime(LocalDateTime.now());
        this.updateById(task);
        recordTrace(taskId, "APPROVE", task.getAssigneeId(), task.getAssigneeName(), comment);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectTask(Long taskId, String comment) {
        WfTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND.getCode(), "任务不存在");
        }
        verifyAssignee(task);
        task.setStatus(TaskStatus.REJECTED.name());
        this.updateById(task);
        recordTrace(taskId, "REJECT", task.getAssigneeId(), task.getAssigneeName(), comment);
        return true;
    }

    @Override
    public List<WfTask> getPendingTasks(Long assigneeId) {
        return this.baseMapper.selectPendingByAssigneeId(assigneeId);
    }

    @Override
    public boolean checkPreTasks(Long taskId) {
        WfTask task = this.getById(taskId);
        if (task == null) return false;
        if (task.getPreTaskIds() == null || task.getPreTaskIds().isEmpty()) {
            return true;
        }
        String[] preTaskIdArr = task.getPreTaskIds().split(",");
        List<Long> preTaskIdList = new java.util.ArrayList<>();
        for (String preTaskIdStr : preTaskIdArr) {
            try {
                preTaskIdList.add(Long.parseLong(preTaskIdStr.trim()));
            } catch (NumberFormatException e) {
                return false;
            }
        }
        // 一次查询所有前置任务，内存比对
        List<WfTask> preTasks = this.listByIds(preTaskIdList);
        for (Long id : preTaskIdList) {
            boolean found = preTasks.stream().anyMatch(t -> id.equals(t.getId()) && TaskStatus.COMPLETED.name().equals(t.getStatus()));
            if (!found) return false;
        }
        return true;
    }

    @Override
    public List<WfTaskTrace> getTaskTraces(Long taskId) {
        LambdaQueryWrapper<WfTaskTrace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskTrace::getTaskId, taskId);
        wrapper.orderByAsc(WfTaskTrace::getOperateTime);
        return taskTraceMapper.selectList(wrapper);
    }

    @Override
    public List<WfTask> getOverdueTasks() {
        LambdaQueryWrapper<WfTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTask::getStatus, TaskStatus.PENDING.name())
               .or()
               .eq(WfTask::getStatus, TaskStatus.IN_PROGRESS.name());
        wrapper.lt(WfTask::getSlaDeadline, LocalDateTime.now());
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public void markOverdueTasks() {
        List<WfTask> overdueTasks = getOverdueTasks();
        for (WfTask task : overdueTasks) {
            task.setStatus(TaskStatus.OVERDUE.name());
            this.updateById(task);
            recordTrace(task.getId(), "SLA_EXPIRED", task.getAssigneeId(), task.getAssigneeName(), "任务逾期");
        }
    }

    private void recordTrace(Long taskId, String action, Long operatorId, String operatorName, String comment) {
        WfTaskTrace trace = new WfTaskTrace();
        trace.setTaskId(taskId);
        trace.setAction(action);
        trace.setOperatorId(operatorId);
        trace.setOperatorName(operatorName);
        trace.setOperateTime(LocalDateTime.now());
        trace.setComment(comment);
        taskTraceMapper.insert(trace);
    }
}
