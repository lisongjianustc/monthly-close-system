package com.mc.common.enums;

/**
 * 节点类型枚举
 */
public enum NodeType {
    ONE_TIME_TASK("one_time_task", "一次性任务"),
    PERIODIC_TASK("periodic_task", "周期任务"),
    IMPORT_CHECK_TASK("import_check_task", "导入校验任务"),
    CLOSE_CONFIRM_TASK("close_confirm_task", "关账确认任务");

    private final String code;
    private final String desc;

    NodeType(String code, String desc) {
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
