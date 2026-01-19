package com.example.citp.service;

import com.example.citp.model.vo.ModulePermissionVO;
import com.example.citp.model.vo.UserModuleVO;

import java.util.List;

/**
 * 模块服务接口
 */
public interface ModuleService {

    /**
     * 获取所有模块及其权限配置
     */
    List<ModulePermissionVO> getAllModulesWithPermissions();

    /**
     * 更新角色模块权限
     */
    void updateRoleModulePermission(Integer roleType, Long moduleId, Integer enabled);

    /**
     * 获取用户可见的模块列表
     */
    List<UserModuleVO> getUserVisibleModules();
}
