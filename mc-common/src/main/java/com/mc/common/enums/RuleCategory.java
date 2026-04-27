package com.mc.common.enums;

/**
 * 规则类别枚举
 */
public enum RuleCategory {
    THRESHOLD("threshold", "阈值校验"),
    YOY("yoy", "同比校验"),
    MOM("mom", "环比校验"),
    INTRA_TABLE("intra_table", "表内勾稽"),
    INTER_TABLE("inter_table", "表间勾稽"),
    FORMULA("formula", "自定义公式");

    private final String code;
    private final String desc;

    RuleCategory(String code, String desc) {
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
