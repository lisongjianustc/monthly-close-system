package com.mc.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.workflow.entity.WfTaskTrace;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务轨迹Mapper
 */
@Mapper
public interface WfTaskTraceMapper extends BaseMapper<WfTaskTrace> {
}