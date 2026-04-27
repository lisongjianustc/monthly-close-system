package com.mc.common.enums;

/**
 * 规则集状态枚举
 */
public enum RuleSetStatus {
    DRAFT("draft", "草稿"),
    PUBLISHED("published", "已发布");

    private final String code;
    private final String desc;

    RuleSetStatus(String code, String desc) {
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
