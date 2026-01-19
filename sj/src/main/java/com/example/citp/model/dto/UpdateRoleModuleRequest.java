package com.example.citp.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新角色模块权限请求
 */
@Data
public class UpdateRoleModuleRequest {

    /**
     * 角色类型：1学生 2教师 3管理员
     */
    @NotNull(message = "角色类型不能为空")
    private Integer roleType;

    /**
     * 模块ID
     */
    @NotNull(message = "模块ID不能为空")
    private Long moduleId;

    /**
     * 是否启用：0禁用 1启用
     */
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;
}
