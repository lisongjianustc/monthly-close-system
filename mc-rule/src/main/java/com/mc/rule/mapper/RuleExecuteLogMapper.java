package com.mc.rule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.rule.entity.RuleExecuteLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 规则执行日志Mapper
 */
@Mapper
public interface RuleExecuteLogMapper extends BaseMapper<RuleExecuteLog> {

    /**
     * 查询规则的执行日志
     */
    List<RuleExecuteLog> selectByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 查询最近的执行日志
     */
    List<RuleExecuteLog> selectRecentLogs(@Param("limit") Integer limit);
}
