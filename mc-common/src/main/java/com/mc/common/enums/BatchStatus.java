package com.mc.common.enums;

/**
 * 导入批次状态枚举
 */
public enum BatchStatus {
    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    COMPLETED("COMPLETED", "已完成"),
    PARTIAL_SUCCESS("PARTIAL_SUCCESS", "部分成功"),
    FAILED("FAILED", "失败"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    BatchStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static BatchStatus getByCode(String code) {
        for (BatchStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
