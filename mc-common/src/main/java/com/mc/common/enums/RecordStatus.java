package com.mc.common.enums;

/**
 * 导入记录状态枚举
 */
public enum RecordStatus {
    PENDING("PENDING", "待处理"),
    VALID("VALID", "已校验"),
    INVALID("INVALID", "无效");

    private final String code;
    private final String desc;

    RecordStatus(String code, String desc) {
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
