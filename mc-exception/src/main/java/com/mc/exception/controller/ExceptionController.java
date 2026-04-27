package com.mc.exception.controller;

import com.mc.common.annotation.Audit;
import com.mc.common.result.Result;
import com.mc.exception.entity.BusinessExceptionRecord;
import com.mc.exception.service.IExceptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 异常处理Controller
 */
@Tag(name = "异常处理管理")
@RestController
@RequestMapping("/exception")
public class ExceptionController {

    @Autowired
    private IExceptionService exceptionService;

    @Audit(action = "记录异常")
    @Operation(summary = "记录异常")
    @PostMapping
    public Result<Long> record(@RequestBody BusinessExceptionRecord record) {
        Long id = exceptionService.recordException(
            record.getExceptionCode(),
            record.getExceptionType(),
            record.getSeverity(),
            record.getModule(),
            record.getBusinessId(),
            record.getPeriodId(),
            record.getUnitId(),
            record.getMessage(),
            record.getStackTrace()
        );
        return Result.success(id);
    }

    @Operation(summary = "查询待处理异常")
    @GetMapping("/pending")
    public Result<List<BusinessExceptionRecord>> getPending(@RequestParam(required = false) Long periodId,
                                                             @RequestParam(required = false) Long unitId) {
        return Result.success(exceptionService.listPendingExceptions(periodId, unitId));
    }

    @Operation(summary = "查询异常详情")
    @GetMapping("/{id}")
    public Result<BusinessExceptionRecord> getDetail(@PathVariable Long id) {
        return Result.success(exceptionService.getExceptionDetail(id));
    }

    @Audit(action = "处理异常")
    @Operation(summary = "处理异常")
    @PostMapping("/handle/{id}")
    public Result<Boolean> handle(@PathVariable Long id,
                                   @RequestParam Long handlerId,
                                   @RequestParam String handlerName,
                                   @RequestParam(required = false) String comment) {
        return Result.success(exceptionService.handleException(id, handlerId, handlerName, comment));
    }

    @Audit(action = "忽略异常")
    @Operation(summary = "忽略异常")
    @PostMapping("/ignore/{id}")
    public Result<Boolean> ignore(@PathVariable Long id, @RequestParam String reason) {
        return Result.success(exceptionService.ignoreException(id, reason));
    }

    @Operation(summary = "异常统计")
    @GetMapping("/statistics")
    public Result<Map<String, Long>> statistics(@RequestParam(required = false) Long periodId,
                                                 @RequestParam(required = false) Long unitId) {
        return Result.success(exceptionService.statisticsException(periodId, unitId));
    }

    @Audit(action = "批量处理异常")
    @Operation(summary = "批量处理")
    @PostMapping("/batch/handle")
    public Result<Boolean> batchHandle(@RequestBody List<Long> ids,
                                        @RequestParam Long handlerId,
                                        @RequestParam String handlerName) {
        return Result.success(exceptionService.batchHandle(ids, handlerId, handlerName));
    }
}
