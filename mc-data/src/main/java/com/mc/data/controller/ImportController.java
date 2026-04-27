package com.mc.data.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.data.dto.ImportProgress;
import com.mc.data.entity.ImportBatch;
import com.mc.data.entity.ImportRecord;
import com.mc.data.service.IImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

/**
 * 数据导入Controller
 */
@Tag(name = "数据导入管理")
@RestController
@RequestMapping("/data/import")
public class ImportController {

    @Autowired
    private IImportService importService;

@Audit(action = "上传文件")
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<Long> upload(@RequestParam MultipartFile file,
                               @RequestParam(required = false, defaultValue = "EXCEL") String importType,
                               @RequestParam(required = false) Long userId,
                               @RequestParam(required = false) String userName) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                return Result.fail("文件名不能为空");
            }
            // 防止路径穿越：过滤非法字符，仅保留安全文件名
            String safeFileName = FilenameUtils.getName(originalFilename);
            if (safeFileName.isEmpty() || safeFileName.contains("..")) {
                return Result.fail("文件名非法");
            }
            // 重新生成文件名，避免覆盖和路径穿越
            String savedFileName = UUID.randomUUID().toString() + "_" + safeFileName;
            String filePath = "/tmp/" + savedFileName;
            // 保存文件到磁盘
            file.transferTo(new File(filePath));
            Long batchId = importService.createBatch(
                importType,
                originalFilename,
                filePath,
                file.getSize(),
                userId != null ? userId : 1L,
                userName != null ? userName : "系统管理员"
            );
            return Result.success(batchId);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Audit(action = "执行导入")
    @Operation(summary = "执行导入")
    @PostMapping("/execute/{batchId}")
    public Result<Boolean> execute(@PathVariable Long batchId,
                                  @RequestParam(required = false) String templateCode) {
        try {
            importService.executeImport(batchId, templateCode);
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Operation(summary = "查询导入进度")
    @GetMapping("/progress/{batchId}")
    public Result<ImportProgress> getProgress(@PathVariable Long batchId) {
        return Result.success(importService.getImportProgress(batchId));
    }

    @Operation(summary = "查询批次记录")
    @GetMapping("/records/{batchId}")
    public Result<List<ImportRecord>> getRecords(@PathVariable Long batchId) {
        return Result.success(importService.getBatchRecords(batchId));
    }

    @Operation(summary = "查询失败记录")
    @GetMapping("/failures/{batchId}")
    public Result<List<ImportRecord>> getFailures(@PathVariable Long batchId) {
        return Result.success(importService.getFailRecords(batchId));
    }

    @Audit(action = "校验数据")
    @Operation(summary = "校验数据")
    @PostMapping("/validate/{batchId}")
    public Result<Boolean> validate(@PathVariable Long batchId) {
        try {
            importService.validateData(batchId);
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Audit(action = "取消导入")
    @Operation(summary = "取消导入")
    @PostMapping("/cancel/{batchId}")
    public Result<Boolean> cancel(@PathVariable Long batchId) {
        return Result.success(importService.cancelImport(batchId));
    }
}
