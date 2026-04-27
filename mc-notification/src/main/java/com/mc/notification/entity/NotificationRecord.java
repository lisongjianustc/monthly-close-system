package com.mc.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_record")
public class NotificationRecord extends NotificationBaseEntity {

    private String templateCode;

    private String channel;

    private String recipient;

    private String subject;

    private String content;

    private String status;

    private Integer retryCount;

    private String errorMsg;

    private LocalDateTime sendTime;

    private LocalDateTime readTime;
}