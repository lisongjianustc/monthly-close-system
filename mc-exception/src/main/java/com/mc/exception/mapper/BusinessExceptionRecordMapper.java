package com.mc.exception.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.exception.entity.BusinessExceptionRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 业务异常记录Mapper
 */
@Mapper
public interface BusinessExceptionRecordMapper extends BaseMapper<BusinessExceptionRecord> {
}
