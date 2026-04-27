package com.mc.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 通知发送请求
 */
@Data
@Schema(name = "通知发送请求")
public class NotificationSendRequest {

    @Schema(name = "模板编码")
    private String templateCode;

    @Schema(name = "通知渠道: EMAIL, WEB_SOCKET")
    private String channel;

    @Schema(name = "收件人")
    private String recipient;

    @Schema(name = "主题")
    private String subject;

    @Schema(name = "内容")
    private String content;

    @Schema(name = "变量参数")
    private Map<String, Object> variables;

    @Schema(name = "关联业务ID")
    private String businessId;

    @Schema(name = "期间ID")
    private Long periodId;

    @Schema(name = "单位ID")
    private Long unitId;
}