package com.example.citp.controller;

import com.example.citp.common.Result;
import com.example.citp.model.dto.UpdateRoleModuleRequest;
import com.example.citp.model.vo.ModulePermissionVO;
import com.example.citp.model.vo.UserModuleVO;
import com.example.citp.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模块管理控制器
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "模块管理", description = "模块权限相关接口")
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping("/admin/modules")
    @Operation(summary = "获取所有模块及权限配置（管理员专用）")
    public Result<List<ModulePermissionVO>> getAllModulesWithPermissions() {
        List<ModulePermissionVO> modules = moduleService.getAllModulesWithPermissions();
        return Result.success(modules);
    }

    @PutMapping("/admin/modules/role-permission")
    @Operation(summary = "更新角色模块权限（管理员专用）")
    public Result<Void> updateRoleModulePermission(@Valid @RequestBody UpdateRoleModuleRequest request) {
        moduleService.updateRoleModulePermission(request.getRoleType(), request.getModuleId(), request.getEnabled());
        return Result.successMsg("权限更新成功");
    }

    @GetMapping("/user/modules")
    @Operation(summary = "获取用户可见的模块列表")
    public Result<List<UserModuleVO>> getUserVisibleModules() {
        List<UserModuleVO> modules = moduleService.getUserVisibleModules();
        return Result.success(modules);
    }
}
