package com.mc.common.enums;

/**
 * 规则执行结果枚举
 */
public enum RuleResult {
    HIT("hit", "命中"),
    MISS("miss", "未命中"),
    ERROR("error", "执行错误");

    private final String code;
    private final String desc;

    RuleResult(String code, String desc) {
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
