package com.mc.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导入模板配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("import_template")
public class ImportTemplate extends DataBaseEntity {

    /** 模板编码 */
    private String code;

    /** 模板名称 */
    private String name;

    /** 模板类型: EXCEL, CSV */
    private String templateType;

    /** 状态: DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已停用 */
    private String status;

    /** 表头映射JSON */
    private String headerMappings;

    /** 数据起始行 */
    private Integer dataStartRow;

    /** 字段校验规则JSON */
    private String validationRules;

    /** 模板文件路径 */
    private String filePath;

    /** 备注 */
    private String remark;
}
