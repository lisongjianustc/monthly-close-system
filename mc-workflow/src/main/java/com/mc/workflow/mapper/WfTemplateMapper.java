package com.mc.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.workflow.entity.WfTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程模板Mapper
 */
@Mapper
public interface WfTemplateMapper extends BaseMapper<WfTemplate> {
}