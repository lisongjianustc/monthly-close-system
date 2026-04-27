package com.mc.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.workflow.entity.WfTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务实例Mapper
 */
@Mapper
public interface WfTaskMapper extends BaseMapper<WfTask> {

    /**
     * 查询待办任务
     */
    List<WfTask> selectPendingByAssigneeId(@Param("assigneeId") Long assigneeId);

    /**
     * 查询实例所有任务
     */
    List<WfTask> selectByInstanceId(@Param("instanceId") Long instanceId);
}