package com.mc.notification.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.notification.dto.NotificationSendRequest;
import com.mc.notification.entity.NotificationRecord;
import com.mc.notification.entity.NotificationTemplate;
import com.mc.notification.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知Controller
 */
@Tag(name = "通知管理")
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    @Audit(action = "发送通知")
    @Operation(summary = "发送通知")
    @PostMapping("/send")
    public Result<Boolean> send(@RequestBody NotificationSendRequest request) {
        notificationService.sendNotification(request);
        return Result.success(true);
    }

    @Operation(summary = "获取模板")
    @GetMapping("/template/{code}")
    public Result<NotificationTemplate> getTemplate(@PathVariable String code) {
        return Result.success(notificationService.getTemplateByCode(code));
    }

    @Operation(summary = "模板列表")
    @GetMapping("/template/list")
    public Result<List<NotificationTemplate>> listTemplates(
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String channel) {
        return Result.success(notificationService.listTemplates(templateType, channel));
    }

    @Audit(action = "创建模板")
    @Operation(summary = "创建模板")
    @PostMapping("/template")
    public Result<Boolean> createTemplate(@RequestBody NotificationTemplate template) {
        return Result.success(notificationService.saveTemplate(template));
    }

    @Audit(action = "更新模板")
    @Operation(summary = "更新模板")
    @PutMapping("/template")
    public Result<Boolean> updateTemplate(@RequestBody NotificationTemplate template) {
        return Result.success(notificationService.updateTemplate(template));
    }

    @Audit(action = "删除模板")
    @Operation(summary = "删除模板")
    @DeleteMapping("/template/{id}")
    public Result<Boolean> deleteTemplate(@PathVariable Long id) {
        return Result.success(notificationService.deleteTemplate(id));
    }

    @Operation(summary = "通知记录列表")
    @GetMapping("/record/list")
    public Result<List<NotificationRecord>> listRecords(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long unitId) {
        return Result.success(notificationService.listRecords(status, channel, periodId, unitId));
    }

    @Audit(action = "重试发送")
    @Operation(summary = "重试发送")
    @PostMapping("/retry/{id}")
    public Result<Boolean> retry(@PathVariable Long id) {
        return Result.success(notificationService.retrySend(id));
    }

    @Operation(summary = "通知统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long unitId) {
        return Result.success(notificationService.getStatistics(periodId, unitId));
    }
}
