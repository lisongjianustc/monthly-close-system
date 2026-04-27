package com.mc.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.common.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志Mapper
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
