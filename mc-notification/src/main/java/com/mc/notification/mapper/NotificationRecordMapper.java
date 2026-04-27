package com.mc.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.notification.entity.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知记录Mapper
 */
@Mapper
public interface NotificationRecordMapper extends BaseMapper<NotificationRecord> {
}