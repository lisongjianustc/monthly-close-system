package com.mc.data.dto;

import lombok.Data;

/**
 * 表头映射DTO
 */
@Data
public class HeaderMapping {

    /** Excel列名 */
    private String excelHeader;

    /** 目标字段编码 */
    private String fieldCode;

    /** 目标字段名称 */
    private String fieldName;

    /** 是否必填 */
    private Boolean required;

    /** 数据类型: STRING, NUMBER, DATE */
    private String dataType;

    /** 默认值 */
    private String defaultValue;

    /** 校验规则 */
    private String validateRule;
}
