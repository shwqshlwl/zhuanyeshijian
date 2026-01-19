package com.example.citp.model.vo;

import lombok.Data;

/**
 * 用户模块VO
 */
@Data
public class UserModuleVO {

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
}
