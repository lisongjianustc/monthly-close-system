package com.mc.common.enums;

/**
 * 流程模板状态枚举
 */
public enum WorkflowTemplateStatus {
    DRAFT("draft", "草稿"),
    PUBLISHED("published", "已发布"),
    DEPRECATED("deprecated", "已停用");

    private final String code;
    private final String desc;

    WorkflowTemplateStatus(String code, String desc) {
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
