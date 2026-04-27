package com.mc.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_template")
public class NotificationTemplate extends NotificationBaseEntity {

    private String code;

    private String name;

    private String templateType;

    private String channel;

    private String subject;

    private String content;

    private String variables;

    private String status;
}