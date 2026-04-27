package com.mc.common.enums;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    PENDING("pending", "待执行"),
    IN_PROGRESS("in_progress", "执行中"),
    PENDING_REVIEW("pending_review", "待复核"),
    PENDING_APPROVE("pending_approve", "待审核"),
    COMPLETED("completed", "已完成"),
    REJECTED("rejected", "已退回"),
    OVERDUE("overdue", "已逾期"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String desc;

    TaskStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskStatus getByCode(String code) {
        for (TaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
