package com.mc.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 父节点ID */
    private Long parentId;

    /** 菜单编码 */
    private String code;

    /** 菜单名称 */
    private String name;

    /** 菜单类型: CATALOG-目录, MENU-菜单, BUTTON-按钮 */
    private String type;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识 */
    private String permission;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 子菜单 (非数据库字段) */
    @TableField(exist = false)
    private List<SysMenu> children;
}