package com.mc.notification.service;

import com.mc.notification.dto.NotificationSendRequest;
import com.mc.notification.entity.NotificationRecord;
import com.mc.notification.entity.NotificationTemplate;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 */
public interface INotificationService {

    void sendNotification(NotificationSendRequest request);

    NotificationTemplate getTemplateByCode(String code);

    List<NotificationTemplate> listTemplates(String templateType, String channel);

    boolean saveTemplate(NotificationTemplate template);

    boolean updateTemplate(NotificationTemplate template);

    boolean deleteTemplate(Long id);

    List<NotificationRecord> listRecords(String status, String channel, Long periodId, Long unitId);

    boolean retrySend(Long id);

    Map<String, Object> getStatistics(Long periodId, Long unitId);
}