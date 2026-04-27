package com.mc.common.enums;

/**
 * 异常严重级别枚举
 */
public enum Severity {
    FATAL("fatal", "致命", "阻断节点完成，必须整改或特批"),
    IMPORTANT("important", "重要", "需说明并复核"),
    WARNING("warning", "警告", "提示，可选处理");

    private final String code;
    private final String desc;
    private final String suggestion;

    Severity(String code, String desc, String suggestion) {
        this.code = code;
        this.desc = desc;
        this.suggestion = suggestion;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
