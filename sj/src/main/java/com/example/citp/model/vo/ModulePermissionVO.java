package com.example.citp.model.vo;

import lombok.Data;

import java.util.Map;

/**
 * 模块权限VO
 */
@Data
public class ModulePermissionVO {

    /**
     * 模块ID
     */
    private Long id;

    /**
     * 模块标识
     */
    private String moduleKey;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 图标
     */
    private String icon;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 各角色的启用状态
     * key: 角色类型（1学生 2教师 3管理员）
     * value: 是否启用（0禁用 1启用）
     */
    private Map<Integer, Integer> rolePermissions;
}
