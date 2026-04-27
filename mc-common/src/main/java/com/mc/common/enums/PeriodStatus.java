package com.mc.common.enums;

/**
 * 期间状态枚举
 */
public enum PeriodStatus {
    ACTIVE("active", "进行中"),
    CLOSED("closed", "已关账"),
    ARCHIVED("archived", "已归档");

    private final String code;
    private final String desc;

    PeriodStatus(String code, String desc) {
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
