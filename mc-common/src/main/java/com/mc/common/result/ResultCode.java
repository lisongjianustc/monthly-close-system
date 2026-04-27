package com.mc.common.result;

/**
 * 结果码枚举
 */
public enum ResultCode {
    SUCCESS(0, "操作成功"),
    FAIL(-1, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    VALIDATE_FAIL(400, "参数校验失败"),
    SYSTEM_ERROR(500, "系统错误"),

    // 流程引擎 1001-1099
    WORKFLOW_NOT_FOUND(1001, "流程模板不存在"),
    WORKFLOW_ALREADY_PUBLISHED(1002, "流程模板已发布"),
    WORKFLOW_HAS_INSTANCES(1003, "流程模板已被使用，不能删除"),
    INSTANCE_NOT_FOUND(2001, "流程实例不存在"),
    TASK_NOT_FOUND(3001, "任务不存在"),
    TASK_CANNOT_TRANSFER(3002, "任务状态不允许此操作"),
    TASK_PRECONDITION_NOT_MET(3003, "前置任务未完成"),

    // 数据导入 4001-4099
    IMPORT_FILE_EMPTY(4001, "导入文件为空"),
    IMPORT_FILE_TOO_LARGE(4002, "文件大小超过限制"),
    IMPORT_FILE_TYPE_NOT_SUPPORT(4003, "文件类型不支持"),
    IMPORT_HEADER_MISSING(4004, "缺少必填字段"),
    IMPORT_DATA_INVALID(4005, "数据格式错误"),
    TEMPLATE_NOT_FOUND(5001, "报表模板不存在"),
    BATCH_NOT_FOUND(5002, "导入批次不存在"),

    // 规则引擎 6001-6099
    RULE_NOT_FOUND(6001, "规则不存在"),
    RULE_SET_NOT_FOUND(6002, "规则集不存在"),
    RULE_EXPRESSION_ERROR(6003, "规则表达式错误"),
    RULE_EXECUTION_ERROR(6004, "规则执行错误"),

    // 异常中心 7001-7099
    EXCEPTION_NOT_FOUND(7001, "异常单不存在"),
    EXCEPTION_CANNOT_CLOSE(7002, "异常单不满足关闭条件"),
    EXCEPTION_HAS_FATAL(7003, "存在未关闭的致命异常"),

    // 通知 8001-8099
    NOTIFICATION_NOT_FOUND(8001, "消息不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
