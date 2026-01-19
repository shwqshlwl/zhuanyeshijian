package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.SysModuleMapper;
import com.example.citp.mapper.SysRoleModuleMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.entity.SysModule;
import com.example.citp.model.entity.SysRoleModule;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.ModulePermissionVO;
import com.example.citp.model.vo.UserModuleVO;
import com.example.citp.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模块服务实现类
 */
@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final SysModuleMapper sysModuleMapper;
    private final SysRoleModuleMapper sysRoleModuleMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<ModulePermissionVO> getAllModulesWithPermissions() {
        // 查询所有模块
        List<SysModule> modules = sysModuleMapper.selectList(
                new LambdaQueryWrapper<SysModule>().orderByAsc(SysModule::getSortOrder)
        );

        // 查询所有角色模块关联
        List<SysRoleModule> roleModules = sysRoleModuleMapper.selectList(null);

        // 构建模块ID -> 角色权限映射
        Map<Long, Map<Integer, Integer>> moduleRolePermissions = new HashMap<>();
        for (SysRoleModule rm : roleModules) {
            moduleRolePermissions
                    .computeIfAbsent(rm.getModuleId(), k -> new HashMap<>())
                    .put(rm.getRoleType(), rm.getEnabled());
        }

        // 构建返回结果
        return modules.stream().map(module -> {
            ModulePermissionVO vo = BeanUtil.copyProperties(module, ModulePermissionVO.class);

            // 获取该模块的角色权限，如果不存在则默认为0（禁用）
            Map<Integer, Integer> permissions = moduleRolePermissions.getOrDefault(module.getId(), new HashMap<>());
            Map<Integer, Integer> allRolePermissions = new HashMap<>();
            allRolePermissions.put(1, permissions.getOrDefault(1, 0)); // 学生
            allRolePermissions.put(2, permissions.getOrDefault(2, 0)); // 教师
            allRolePermissions.put(3, permissions.getOrDefault(3, 0)); // 管理员

            vo.setRolePermissions(allRolePermissions);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleModulePermission(Integer roleType, Long moduleId, Integer enabled) {
        // 验证模块是否存在
        SysModule module = sysModuleMapper.selectById(moduleId);
        if (module == null) {
            throw new BusinessException("模块不存在");
        }

        // 验证角色类型
        if (roleType < 1 || roleType > 3) {
            throw new BusinessException("无效的角色类型");
        }

        // 验证启用状态
        if (enabled != 0 && enabled != 1) {
            throw new BusinessException("无效的启用状态");
        }

        // 特殊保护：管理员的模块管理权限不能被禁用
        if (roleType == 3 && "admin-modules".equals(module.getModuleKey()) && enabled == 0) {
            throw new BusinessException("不能禁用管理员的模块管理权限");
        }

        // 查询是否已存在该权限配置
        SysRoleModule existRoleModule = sysRoleModuleMapper.selectOne(
                new LambdaQueryWrapper<SysRoleModule>()
                        .eq(SysRoleModule::getRoleType, roleType)
                        .eq(SysRoleModule::getModuleId, moduleId)
        );

        if (existRoleModule != null) {
            // 更新
            existRoleModule.setEnabled(enabled);
            sysRoleModuleMapper.updateById(existRoleModule);
        } else {
            // 新增
            SysRoleModule newRoleModule = new SysRoleModule();
            newRoleModule.setRoleType(roleType);
            newRoleModule.setModuleId(moduleId);
            newRoleModule.setEnabled(enabled);
            sysRoleModuleMapper.insert(newRoleModule);
        }
    }

    @Override
    public List<UserModuleVO> getUserVisibleModules() {
        SysUser user = getCurrentUser();

        // 查询该角色可见的模块ID列表
        List<SysRoleModule> roleModules = sysRoleModuleMapper.selectList(
                new LambdaQueryWrapper<SysRoleModule>()
                        .eq(SysRoleModule::getRoleType, user.getUserType())
                        .eq(SysRoleModule::getEnabled, 1)
        );

        if (roleModules.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取模块ID列表
        List<Long> moduleIds = roleModules.stream()
                .map(SysRoleModule::getModuleId)
                .collect(Collectors.toList());

        // 查询模块详情
        List<SysModule> modules = sysModuleMapper.selectBatchIds(moduleIds);

        // 按排序字段排序并转换为VO
        return modules.stream()
                .sorted(Comparator.comparing(SysModule::getSortOrder))
                .map(module -> BeanUtil.copyProperties(module, UserModuleVO.class))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前登录用户
     */
    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            throw new BusinessException(401, "未登录");
        }

        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername();

        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user;
    }
}
