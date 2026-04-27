package com.mc.common.enums;

/**
 * 异常单状态枚举
 */
public enum ExceptionStatus {
    OPEN("open", "待处理"),
    IN_PROGRESS("in_progress", "整改中"),
    PENDING_REVIEW("pending_review", "待复核"),
    CLOSED("closed", "已关闭"),
    AUTO_RESOLVED("auto_resolved", "自动消除");

    private final String code;
    private final String desc;

    ExceptionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
