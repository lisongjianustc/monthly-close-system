package com.mc.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.notification.dto.NotificationSendRequest;
import com.mc.notification.entity.NotificationRecord;
import com.mc.notification.entity.NotificationTemplate;
import com.mc.notification.mapper.NotificationRecordMapper;
import com.mc.notification.mapper.NotificationTemplateMapper;
import com.mc.notification.service.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务实现
 */
@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationTemplateMapper, NotificationTemplate>
        implements INotificationService {

    @Autowired
    private NotificationRecordMapper recordMapper;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    @Transactional
    public void sendNotification(NotificationSendRequest request) {
        NotificationRecord record = new NotificationRecord();
        record.setTemplateCode(request.getTemplateCode());
        record.setChannel(request.getChannel());
        record.setRecipient(request.getRecipient());
        record.setSubject(request.getSubject() != null ? request.getSubject() : "Monthly Close Notification");
        record.setContent(request.getContent());
        record.setStatus("PENDING");
        record.setRetryCount(0);

        try {
            if ("EMAIL".equals(request.getChannel())) {
                sendEmail(request, record);
            } else if ("WEB_SOCKET".equals(request.getChannel())) {
                sendWebSocket(request, record);
            } else {
                throw new IllegalArgumentException("Unsupported channel: " + request.getChannel());
            }
            record.setStatus("SENT");
            record.setSendTime(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            record.setStatus("FAILED");
            record.setErrorMsg(e.getMessage());
        }

        recordMapper.insert(record);
    }

    private void sendEmail(NotificationSendRequest request, NotificationRecord record) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping email send");
            record.setStatus("SKIPPED");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getRecipient());
        message.setSubject(request.getSubject() != null ? request.getSubject() : "Monthly Close Notification");
        message.setText(request.getContent());
        message.setFrom("noreply@monthlyclose.com");

        mailSender.send(message);
        log.info("Email sent to: {}", request.getRecipient());
    }

    private void sendWebSocket(NotificationSendRequest request, NotificationRecord record) {
        log.info("WebSocket notification prepared for: {}, content: {}",
                request.getRecipient(), request.getContent());
        record.setStatus("SENT");
        record.setSendTime(LocalDateTime.now());
    }

    @Override
    public NotificationTemplate getTemplateByCode(String code) {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getCode, code);
        return this.getOne(wrapper);
    }

    @Override
    public List<NotificationTemplate> listTemplates(String templateType, String channel) {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        if (templateType != null) {
            wrapper.eq(NotificationTemplate::getTemplateType, templateType);
        }
        if (channel != null) {
            wrapper.eq(NotificationTemplate::getChannel, channel);
        }
        wrapper.orderByDesc(NotificationTemplate::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public boolean saveTemplate(NotificationTemplate template) {
        template.setStatus("ACTIVE");
        return this.save(template);
    }

    @Override
    @Transactional
    public boolean updateTemplate(NotificationTemplate template) {
        return this.updateById(template);
    }

    @Override
    @Transactional
    public boolean deleteTemplate(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<NotificationRecord> listRecords(String status, String channel, Long periodId, Long unitId) {
        LambdaQueryWrapper<NotificationRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(NotificationRecord::getStatus, status);
        }
        if (channel != null) {
            wrapper.eq(NotificationRecord::getChannel, channel);
        }
        wrapper.orderByDesc(NotificationRecord::getCreateTime);
        return recordMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean retrySend(Long id) {
        NotificationRecord record = recordMapper.selectById(id);
        if (record == null) {
            return false;
        }

        NotificationSendRequest request = new NotificationSendRequest();
        request.setTemplateCode(record.getTemplateCode());
        request.setChannel(record.getChannel());
        request.setRecipient(record.getRecipient());
        request.setSubject(record.getSubject());
        request.setContent(record.getContent());

        try {
            if ("EMAIL".equals(record.getChannel())) {
                sendEmail(request, record);
            } else if ("WEB_SOCKET".equals(record.getChannel())) {
                sendWebSocket(request, record);
            }
            record.setStatus("SENT");
            record.setSendTime(LocalDateTime.now());
            record.setRetryCount(record.getRetryCount() + 1);
        } catch (Exception e) {
            record.setStatus("FAILED");
            record.setErrorMsg(e.getMessage());
            record.setRetryCount(record.getRetryCount() + 1);
        }

        recordMapper.updateById(record);
        return true;
    }

    @Override
    public Map<String, Object> getStatistics(Long periodId, Long unitId) {
        Map<String, Object> stats = new HashMap<>();

        // TOTAL
        stats.put("TOTAL", recordMapper.selectCount(null));

        // SENT
        LambdaQueryWrapper<NotificationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRecord::getStatus, "SENT");
        stats.put("SENT", recordMapper.selectCount(wrapper));

        // FAILED
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRecord::getStatus, "FAILED");
        stats.put("FAILED", recordMapper.selectCount(wrapper));

        // PENDING
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRecord::getStatus, "PENDING");
        stats.put("PENDING", recordMapper.selectCount(wrapper));

        return stats;
    }
}
